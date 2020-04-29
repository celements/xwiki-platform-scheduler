package com.celements.cleverreach.util;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.cleverreach.exception.CssInlineException;

@ComponentRole
public interface CssInliner {

  @NotNull
  String inline(@NotNull String html, @NotNull List<String> cssList) throws CssInlineException;

  @NotNull
  String inline(@NotNull String html, @NotNull String css) throws CssInlineException;

}
