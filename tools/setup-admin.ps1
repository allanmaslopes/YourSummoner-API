# Configura a área de admin no .env sem mostrar os segredos na tela.
# Uso: powershell -NoProfile -ExecutionPolicy Bypass -File tools\setup-admin.ps1
# Gera YOURSUMMONER_MASTER_KEY (se ainda não existir), pede usuário e senha e grava o hash BCrypt da senha.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root '.env'
if (-not (Test-Path $envFile)) { New-Item -ItemType File $envFile | Out-Null }
$lines = @(Get-Content $envFile -Encoding UTF8 | Where-Object { $_ -notmatch '^(YOURSUMMONER_ADMIN_USER|YOURSUMMONER_ADMIN_PASSWORD_HASH)=' })

if (-not ($lines | Where-Object { $_ -match '^YOURSUMMONER_MASTER_KEY=.+' })) {
    $bytes = [byte[]]::new(32)
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $lines = @($lines | Where-Object { $_ -notmatch '^YOURSUMMONER_MASTER_KEY=' }) + "YOURSUMMONER_MASTER_KEY=$([Convert]::ToBase64String($bytes))"
    Write-Host 'Chave mestra gerada (guardada só no .env; perder ela = perder o cofre).'
} else {
    Write-Host 'Chave mestra já existe no .env: mantida.'
}

$user = Read-Host 'Usuário admin'
if ([string]::IsNullOrWhiteSpace($user)) { throw 'Usuário vazio.' }
$secure = Read-Host 'Senha admin' -AsSecureString
$plain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR([Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))
if ([string]::IsNullOrWhiteSpace($plain)) { throw 'Senha vazia.' }

$env:JAVA_HOME = 'D:\Workspace\Languages-Frameworks\jdk-25.0.4.1+1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Push-Location $root
try {
    Write-Host 'Gerando o hash BCrypt (compila os testes na primeira vez)...'
    mvn -q test-compile | Out-Null
    $hash = ($plain | mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.0:java "-Dexec.mainClass=com.your.summoner.support.HashPassword" "-Dexec.classpathScope=test") |
        Where-Object { $_ -match '^\$2[aby]\$' } | Select-Object -Last 1
} finally {
    Pop-Location
    $plain = $null
}
if (-not $hash) { throw 'Não consegui gerar o hash BCrypt.' }

# Sem aspas: o .env é lido pelo Spring como .properties (spring.config.import), onde aspas viram parte do valor.
$lines += "YOURSUMMONER_ADMIN_USER=$user"
$lines += "YOURSUMMONER_ADMIN_PASSWORD_HASH=$hash"
# UTF-8 sem BOM: o BOM grudaria no nome da primeira variável.
$utf8 = New-Object System.Text.UTF8Encoding $false
[IO.File]::WriteAllLines($envFile, [string[]]$lines, $utf8)

# O coletor lê o mesmo cofre e precisa da mesma chave mestra.
$collectorEnv = 'D:\Workspace\yoursummoner_collector\.env'
if (Test-Path (Split-Path $collectorEnv)) {
    $master = ($lines | Where-Object { $_ -match '^YOURSUMMONER_MASTER_KEY=' } | Select-Object -First 1)
    $col = @()
    if (Test-Path $collectorEnv) { $col = @(Get-Content $collectorEnv -Encoding UTF8 | Where-Object { $_ -notmatch '^YOURSUMMONER_MASTER_KEY=' }) }
    [IO.File]::WriteAllLines($collectorEnv, [string[]]($col + $master), $utf8)
    Write-Host 'Chave mestra copiada também para o .env do yoursummoner_collector.'
}
Write-Host "Pronto: .env atualizado para o usuário '$user'. Reinicie a API."
