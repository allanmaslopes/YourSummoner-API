package com.yoursummoner.api.enumeration;

public enum RequestApiEnum {

    //Classe enum para direcionar as APIs e páginas a ser consumidas
    BR("https://br1.api.riotgames.com"),
    AMERICAS("https://americas.api.riotgames.com"),
    DDRAGON("http://ddragon.leagueoflegends.com/");
    private final String clientAmbiente;

    RequestApiEnum(String clientAmbiente) {
        this.clientAmbiente = clientAmbiente;
    }

    public String getClientAmbiente() {
        return clientAmbiente;
    }
}
