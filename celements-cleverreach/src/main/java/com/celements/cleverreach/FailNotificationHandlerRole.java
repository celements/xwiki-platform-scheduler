package com.celements.cleverreach;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface FailNotificationHandlerRole {

  /**
   * Sends a mail to all configured mail receivers.
   *
   * @param msg
   *          A description of the error encountered
   * @param excp
   *          The exception
   */
  void send(String msg, Exception excp);

}
