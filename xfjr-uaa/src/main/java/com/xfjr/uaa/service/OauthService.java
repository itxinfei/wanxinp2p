package com.xfjr.uaa.service;


import com.xfjr.uaa.domain.OauthClientDetails;
import com.xfjr.uaa.domain.OauthClientDetailsDto;

import java.util.List;

public interface OauthService {

    OauthClientDetails loadOauthClientDetails(String clientId);

    List<OauthClientDetailsDto> loadAllOauthClientDetailsDtos();

    void archiveOauthClientDetails(String clientId);

    OauthClientDetailsDto loadOauthClientDetailsDto(String clientId);

    void registerClientDetails(OauthClientDetailsDto formDto);
    
}