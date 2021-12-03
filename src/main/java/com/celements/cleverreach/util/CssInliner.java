package com.celements.cleverreach.util;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.cleverreach.exception.CssInlineException;

@ComponentRole
public interface CssInliner {

  @NotNull
  String inline(@NotNull String html, @NotNull List<String> cssList) throws CssInlineException;

  @NotNull
  String inline(@NotNull String html, @NotNull List<String> cssList,
      @Nullable Map<String, String> configs) throws CssInlineException;

  @NotNull
  String inline(@NotNull String html, @NotNull String css) throws CssInlineException;

  @NotNull
  String inline(@NotNull String html, @NotNull String css, @Nullable Map<String, String> configs)
      throws CssInlineException;

}
