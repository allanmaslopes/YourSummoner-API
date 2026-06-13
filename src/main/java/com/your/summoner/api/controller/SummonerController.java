package com.your.summoner.api.controller;

import com.your.summoner.api.model.dto.SummonerProfileDto;
import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.service.SummonerService;
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
@RequestMapping(path = "/summoners", produces = MediaType.APPLICATION_JSON_VALUE)
@Api
public class SummonerController {

    @Autowired
    SummonerService summonerService;

    @GetMapping("/{nick}/{tag}")
    @ApiOperation(value = "Return's summoner profile information", response = SummonerProfileDto.class)
    public ResponseEntity<Object> searchSummoner(@PathVariable String nick, @PathVariable String tag) {
        ResponseEntity<Object> response;

        try {
            response = new ResponseEntity<>(summonerService.findSummonerProfile(nick, tag), HttpStatus.OK);
        } catch (ApiError ex) {
            response = new ResponseEntity<>(ex, ex.getHttpStatus());
        }

        return response;
    }
}//https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/Humanzee/S%C3%ADmio?api_key=RGAPI-3103e905-d5cf-4dfe-9101-cb698db8937c
