package net.deuce.moman.controller;

import net.deuce.moman.om.RepeatingTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RepeatingTransactionController extends AbstractController {

  @Autowired
  private RepeatingTransactionService repeatingTransactionService;

  public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {

    handleDefaultActions(req, res, repeatingTransactionService);

    return null;
  }
}