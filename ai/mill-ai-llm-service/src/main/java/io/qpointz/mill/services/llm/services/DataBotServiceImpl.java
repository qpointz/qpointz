package io.qpointz.mill.services.llm.services;

import io.qpointz.mill.services.annotations.ConditionalOnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnService("data-bot")
public class DataBotServiceImpl implements DataBotService {
}
