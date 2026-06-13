package com.your.summoner.api.controller;

import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.service.ChampionService;
import com.your.summoner.api.service.StaticService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api
public class ChampionController {

    @Autowired
    ChampionService championService;

    @GetMapping("findVersion")
    public ResponseEntity<Object> loadFindVersion() {
        ResponseEntity<Object> response;

        try {
            StaticService.findPatch();
            response = new ResponseEntity<>(StaticService.version, HttpStatus.OK);
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
            response = new ResponseEntity<>(championService.getChampDetail(championName), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;

    }

    @GetMapping("/by-mastery/{nick}/{tag}")
    public ResponseEntity<Object> allChampionsByMastery(@PathVariable String nick, @PathVariable String tag) {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(championService.getChampionsByMastery(nick, tag), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }

    @GetMapping("/summoner/{nick}/tag/{tag}/lane/{lane}")
    public ResponseEntity<Object> allChampionsByMastery(@PathVariable String nick,
                                                        @PathVariable String tag,
                                                        @PathVariable String lane) {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(championService.getFilterSearchChampions(nick, lane, tag), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }
}
