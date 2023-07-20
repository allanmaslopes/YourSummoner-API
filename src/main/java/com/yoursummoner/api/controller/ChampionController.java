package com.yoursummoner.api.controller;

import com.yoursummoner.api.model.error.ApiError;
import com.yoursummoner.api.service.ChampionWS;
import com.yoursummoner.api.service.StaticWS;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping(value = "champions", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class ChampionController {

    @Autowired
    ChampionWS championWS;

    @GetMapping("findVersion")
    public ResponseEntity<Object> loadFindVersion() {
        ResponseEntity<Object> response;

        try {
            StaticWS.findPatch();
            response = new ResponseEntity<>(StaticWS.version, HttpStatus.OK);
        }  catch (Exception ex) {
            response = new ResponseEntity<>(ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @GetMapping("/find-champion/{championName}")
    @ApiOperation("")
    public ResponseEntity<Object> getOneChampion(@PathVariable String championName) {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(championWS.getChampDetail(championName), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;

    }

    @GetMapping("/by-mastery/{nick}")
    public ResponseEntity<Object> allChampionsByMastery(@PathVariable String nick) {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(championWS.getChampionsByMastery(nick), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }

    @GetMapping("/summoner/{nick}/lane/{lane}")
    public ResponseEntity<Object> allChampionsByMastery(@PathVariable String nick,
                                                        @PathVariable String lane) {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(championWS.getFilterSearchChampions(nick, lane), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }
}
