package com.your.summoner.api.controller;

import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.service.MatchService;
import com.your.summoner.api.model.match.MatchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {
        "http://localhost:4200"
}, methods = {
        RequestMethod.GET,
})
@RestController
@RequestMapping(path = "/matchs", produces = MediaType.APPLICATION_JSON_VALUE)
public class MatchController {

    @Autowired
    MatchService matchService;

    @GetMapping("/{nick}")
    public ResponseEntity<Object> searchMatchs(
            @PathVariable String nick,
            @RequestParam(name = "quantity", required = false) Integer quantity,
            @RequestParam(name = "startTime", required = false) Long startTime,
            @RequestParam(name = "endTime", required = false) Long endTime,
            @RequestParam(name = "queue", required = false) Integer queue,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "start", required = false) Integer start) {

        ResponseEntity<Object> response;

        try {
            MatchRequest matchRequest = new MatchRequest(startTime, endTime, queue, type, start, quantity);
            response = new ResponseEntity<>(matchService.getMatchId(nick, matchRequest), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }

    @GetMapping("/matchId/{matchId}")
    public ResponseEntity<Object> oneMatch(@PathVariable String matchId) {

        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(matchService.getMatch(matchId), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }
}
