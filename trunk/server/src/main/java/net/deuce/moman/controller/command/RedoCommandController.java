package net.deuce.moman.controller.command;

import net.deuce.moman.job.Result;
import net.deuce.moman.om.User;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedoCommandController extends AbstractJobCommandController {

  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Result result = redo();
    if (result != null && result.getResult() != null) {
      sendResult(result, response);
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    return null;
  }

  protected Result redo() throws Exception {
    User user = getUserService().getStaticUser();
    return new Result(HttpServletResponse.SC_OK, getUndoManager().redo(user));
  }

}