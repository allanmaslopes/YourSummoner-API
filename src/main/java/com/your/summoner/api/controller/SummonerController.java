package com.your.summoner.api.controller;

import com.your.summoner.api.model.Dto.SummonerProfileDto;
import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.service.SummonerService;
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
@RequestMapping(path = "/summoners", produces = MediaType.APPLICATION_JSON_VALUE)
public class SummonerController {

    @Autowired
    SummonerService summonerService;

    @GetMapping("/{nick}")
    @ApiOperation(value = "Return's summoner profile information", response = SummonerProfileDto.class)
    public ResponseEntity<Object> searchSummoner(@PathVariable String nick) throws ApiError {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(summonerService.findSummoner(nick), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }
}
