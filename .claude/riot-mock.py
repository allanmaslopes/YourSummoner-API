# Mock local da Riot API para testar o front sem chave: serve os fixtures do WireMock dos testes.
import http.server
import pathlib

FILES = pathlib.Path(__file__).resolve().parent.parent / "src/test/resources/wiremock/__files"
ROUTES = {
    "/riot/americas/riot/account/v1/accounts/by-riot-id/Tester/BR1": "account-tester.json",
    "/riot/br1/lol/summoner/v4/summoners/by-puuid/puuid-tester": "summoner-tester.json",
    "/riot/br1/lol/league/v4/entries/by-puuid/puuid-tester": "league-tester.json",
    "/riot/br1/lol/champion-mastery/v4/champion-masteries/by-puuid/puuid-tester": "mastery-tester.json",
    "/riot/americas/lol/match/v5/matches/by-puuid/puuid-tester/ids": "match-ids-tester.json",
    "/riot/americas/lol/match/v5/matches/BR1_3000000001": "match-BR1_3000000001.json",
}


class Handler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        name = ROUTES.get(self.path.split("?")[0])
        if name is None:
            body, status = b'{"status":{"message":"Data not found","status_code":404}}', 404
        else:
            body, status = (FILES / name).read_bytes(), 200
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(body)


http.server.ThreadingHTTPServer(("127.0.0.1", 8089), Handler).serve_forever()
