# Administração de segredos e parâmetros

A API guarda a chave da Riot num cofre criptografado (MongoDB) e expõe `/admin/**` para trocá-la sem reiniciar.
Tudo é controlado por três variáveis de ambiente. **Nunca commite o `.env`** (ele está no `.gitignore`) e nunca cole esses valores em issue, log ou chat.

| Variável | Para quê |
|---|---|
| `YOURSUMMONER_MASTER_KEY` | Chave mestra (Base64 de 32 bytes). Criptografa o cofre (AES-256-GCM) e assina o JWT. Sem ela o cofre e a área admin ficam desligados (`/admin/**` responde 503) e a API usa só `RIOT_API_KEY`. |
| `YOURSUMMONER_ADMIN_USER` | Nome do usuário admin único. |
| `YOURSUMMONER_ADMIN_PASSWORD_HASH` | Hash BCrypt da senha (nunca a senha). Sem ele a área admin fica desligada (503). |

Uma chave mestra que não seja Base64 de exatamente 32 bytes impede a subida da API, com mensagem clara (o valor não é impresso).

## Gerar a chave mestra (PowerShell)

```powershell
$bytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Guarde o resultado só no `.env`. **Perder a chave mestra significa perder o cofre**: os segredos gravados não podem ser decifrados (a API cai no `RIOT_API_KEY` do ambiente) e será preciso cadastrá-los de novo. Trocar a chave mestra também invalida os JWT em circulação (15 min).

## Gerar o hash BCrypt da senha

Opção 1, sem instalar nada além do projeto (digite a senha e Enter; ela não vai para o histórico do shell):

```powershell
$env:JAVA_HOME = "D:\Workspace\Languages-Frameworks\jdk-25.0.4.1+1"
mvn -q test-compile
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.0:java "-Dexec.mainClass=com.your.summoner.support.HashPassword" "-Dexec.classpathScope=test"
```

Opção 2, com `htpasswd` (Apache): `htpasswd -nBC 12 "" | tr -d ':\n' | sed 's/$2y/$2a/'`.

## `.env`

O jeito mais simples: `powershell -NoProfile -ExecutionPolicy Bypass -File tools\setup-admin.ps1` (gera a chave mestra, pede usuário e senha sem mostrar e grava o hash; também copia a chave mestra para o `.env` do `yoursummoner_collector`).

Manualmente: copie `.env.example` para `.env` e preencha. O `.env` é lido pelo Spring como `.properties` (`spring.config.import` no `.claude/run-api.cmd`), então **não** ponha aspas no hash: elas virariam parte do valor.

```
YOURSUMMONER_MASTER_KEY=<base64 de 32 bytes>
YOURSUMMONER_ADMIN_USER=admin
YOURSUMMONER_ADMIN_PASSWORD_HASH=$2a$12$...
```

## Como funciona

- `POST /admin/session` com `{user, password, totp}` devolve um JWT HS256 de 15 min (`Authorization: Bearer`). A chave do JWT é derivada da chave mestra por HKDF-SHA256.
- **Primeiro login** (senha correta, ainda sem TOTP): a API gera o segredo TOTP, guarda criptografado no cofre e devolve `totpEnrollmentUri` (`otpauth://`) uma única vez. Cadastre no autenticador na hora. Daqui em diante o código de 6 dígitos é obrigatório (janela de ±1 passo de 30 s). Se perder o cadastro, apague o documento `ADMIN_TOTP` da coleção `secrets` (o próximo login gera outro).
- Rate limit: 5 logins falhos em 15 min por IP (`429` com `Retry-After`). O contador é em memória e do endereço de socket; `X-Forwarded-For` é ignorado de propósito.
- `/admin/**` só aceita origem em `yoursummoner.admin.allowed-cidrs` (padrão `127.0.0.1/32,::1/128`; fora disso, 403). Atrás de proxy reverso o IP visto é o do proxy: ajuste a lista com cuidado.
- A API nunca devolve o valor de um segredo: `GET /admin/secrets` mostra nome, 4 últimos caracteres, data e autor.
- Auditoria na coleção `admin_audit` (`GET /admin/audit`): login ok/falho, troca de segredo (só o nome) e de parâmetros.
- Parâmetros (`GET/PUT /admin/settings`: `locale`, `defaultPlatform`) ficam na coleção `app_settings`; o `application.yml` é o valor inicial.
- Ordem da chave da Riot: cofre primeiro, `RIOT_API_KEY` se o cofre estiver vazio ou indisponível. A troca vale na próxima chamada.
