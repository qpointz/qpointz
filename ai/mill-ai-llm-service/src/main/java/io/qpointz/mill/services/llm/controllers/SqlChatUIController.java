package io.qpointz.mill.services.llm.controllers;

import io.qpointz.mill.services.annotations.ConditionalOnService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@ConditionalOnService("data-bot")
public class SqlChatUIController {

  @RequestMapping(value = {"/sql-chat", "/sql-chat/"} )
  public void rootRedirect(HttpServletResponse response) throws IOException {
      response.sendRedirect("/sql-chat/index.html");
  }

}
