package com.celements.tag.providers;

import java.util.Collection;

import com.celements.common.lambda.LambdaExceptionUtil.ThrowingSupplier;
import com.celements.tag.CelTag;
import com.celements.tag.providers.CelTagsProvider.CelTagsProvisionException;

public interface CelTagsProvider
    extends ThrowingSupplier<Collection<CelTag.Builder>, CelTagsProvisionException> {

  public class CelTagsProvisionException extends Exception {

    private static final long serialVersionUID = 1L;

    public CelTagsProvisionException(String message) {
      super(message);
    }

    public CelTagsProvisionException(Throwable cause) {
      super(cause);
    }

    public CelTagsProvisionException(String message, Throwable cause) {
      super(message, cause);
    }

  }

}
