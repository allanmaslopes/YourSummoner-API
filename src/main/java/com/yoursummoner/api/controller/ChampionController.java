package com.yoursummoner.api.controller;

import com.yoursummoner.api.model.error.ApiError;
import com.yoursummoner.api.service.ChampionWS;
import com.yoursummoner.api.service.StaticWS;
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
            log.info("New version loaded -> {}", StaticWS.version);
        } catch (ApiError ex) {
            log.error("Error to call method loadFindVersion -> {}, httpstatus -> {}", ex.getMessage(), ex.getHttpStatus());
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error to call method loadFindVersion -> {}", ex.getMessage());
            response = new ResponseEntity<>(ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @GetMapping("/load-championsList")
    public ResponseEntity<Object> loadAllChampions() {
        ResponseEntity<Object> response;

        try {
            StaticWS.findChampionsList();
            response = new ResponseEntity<>(StaticWS.getChampionsList(), StaticWS.getChampionsList().isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
            log.info("Champion list filled, size -> {}",StaticWS.getChampionsList().size());
        } catch (ApiError ex) {
            log.error("Error method loadAllChampions -> {}, status -> {}", ex.getMessage(), ex.getHttpStatus());
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error method loadAllChampions -> {}", ex.getMessage());
            response = new ResponseEntity<>(ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> allChampions() {
        ResponseEntity<Object> response = null;

        try {

            if (StaticWS.getChampionsList().isEmpty()) {
                StaticWS.findChampionsList();
            } else {
                response = new ResponseEntity<>(StaticWS.getChampionsList(), HttpStatus.OK);
            }

        } catch (ApiError ex) {
            log.error("Error method loadAllChampions -> {}, status -> {}", ex.getMessage(), ex.getHttpStatus());
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error method loadAllChampions -> {}", ex.getMessage());
            response = new ResponseEntity<>(ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @GetMapping("/find-champion/{championName}")
    public ResponseEntity<Object> getOneChampion(@PathVariable String championName) {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(championWS.getChampDetail(championName), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;

    }

    @GetMapping("/champions/{nick}")
    public ResponseEntity<Object> allChampionsByMastery(@PathVariable String nick) {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(championWS.getChampionsByMastery(nick), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }

    @GetMapping("/by-lane/{nick}")
    public ResponseEntity<Object> allChampionsByMastery(@PathVariable String nick,
                                                        @RequestParam(name = "lane") String lane) {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(championWS.getFilterSearchChampions(nick, lane), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }
}
