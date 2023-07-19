package com.yoursummoner.api.controller;

import com.yoursummoner.api.model.error.ApiError;
import com.yoursummoner.api.model.match.MatchRequest;
import com.yoursummoner.api.service.MatchWS;
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
    MatchWS matchWS;

    @GetMapping("/{nick}")
    public ResponseEntity<Object> searchMatchs(
            @PathVariable String nick,
            @RequestParam(name = "quantidade", required = false) Integer quantidade,
            @RequestParam(name = "startTime", required = false) Long startTime,
            @RequestParam(name = "endTime", required = false) Long endTime,
            @RequestParam(name = "queue", required = false) Integer queue,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "start", required = false) Integer start) {

        ResponseEntity<Object> response;

        try {
            MatchRequest matchRequest = new MatchRequest(startTime, endTime, queue, type, start, quantidade);
            response = new ResponseEntity<>(matchWS.getMatchId(nick, matchRequest), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }

    @GetMapping("/matchId/{matchId}")
    public ResponseEntity<Object> oneMatch(@PathVariable String matchId) {

        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(matchWS.getMatch(matchId), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }
}
