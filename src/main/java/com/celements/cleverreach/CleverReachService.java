package com.celements.cleverreach;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface CleverReachService {

  /**
   * Update a mailing
   *
   * @param mailing
   *          The mailing needing an update
   * @return true if updated successfully
   * @throws IOException
   */
  boolean updateMailing(@NotNull MailingConfig mailing) throws IOException;

  /**
   * For debugging only. Returns the logged in user.
   *
   * @return The application creator info as JSON
   * @throws IOException
   */
  @NotNull
  String whoami() throws IOException;

  /**
   * Get the TTL for the token.
   *
   * @return The TTL and Expiration date as JSON
   * @throws IOException
   */
  @NotNull
  String ttl() throws IOException;

  /**
   * Get the default configuration document reference.
   *
   * @return DocumentReference of the configuration document
   */
  @NotNull
  DocumentReference getConfigDocRef();

}
