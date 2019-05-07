package com.celements.cleverreach.util;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface CssInliner {

  @NotNull
  String inline(@NotNull String html, @NotNull List<String> cssList);

  @NotNull
  String inline(@NotNull String html, @NotNull String css);

}
