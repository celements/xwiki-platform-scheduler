package com.celements.tag;

import static com.google.common.base.Preconditions.*;
import static java.util.stream.Collectors.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.EntityReference;

import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

import one.util.streamex.StreamEx;

@Immutable
public final class CelTag {

  private static final Supplier<CelTagService> tagService = () -> Utils
      .getComponent(CelTagService.class);

  public static Builder builder() {
    return new Builder();
  }

  private final @NotEmpty String type;
  private final @NotEmpty String name;
  private final @NotNull Optional<EntityReference> scope;
  private final @NotNull Optional<CelTag> parent;
  private final @NotNull List<CelTag> dependencies;
  private final @NotNull Function<String, Optional<String>> prettyNameForLangGetter;

  private CelTag(Builder builder) {
    checkArgument(builder.hasAllDependencies());
    this.type = builder.type;
    checkArgument(!type.isEmpty(), "type cannot be empty");
    this.name = builder.name;
    checkArgument(!name.isEmpty(), "name cannot be empty");
    this.scope = Optional.ofNullable(builder.scope);
    this.parent = Optional.ofNullable(builder.dependencies.get(builder.parent));
    this.dependencies = builder.dependencies.values().stream()
        .filter(Objects::nonNull)
        .filter(tag -> !tag.getName().equals(builder.parent))
        .collect(toUnmodifiableList());
    this.prettyNameForLangGetter = Optional.ofNullable(builder.prettyNameForLangGetter)
        .orElse(lang -> Optional.empty());
  }

  public @NotEmpty String getType() {
    return type;
  }

  public @NotEmpty String getName() {
    return name;
  }

  public @NotNull Optional<EntityReference> getScope() {
    return scope;
  }

  public boolean hasScope(@Nullable EntityReference ref) {
    return getScope()
        .map(s -> (ref != null) && ref.equals(s.extractRef(ref.getType()).orElse(null)))
        .orElse(true); // undefined scope is always in scope
  }

  public @NotNull Stream<CelTag> getParents() {
    return StreamEx.iterate(parent, p -> p.isPresent(), p -> p.get().parent)
        .map(Optional::get);
  }

  public @NotNull Stream<CelTag> getChildren() {
    return tagService.get().getTagsByType()
        .get(getType()).stream()
        .filter(tag -> tag.parent.filter(this::equals).isPresent());
  }

  public @NotNull Stream<CelTag> getAllChildren() {
    return getChildren().flatMap(child -> StreamEx.of(child)
        .append(child.getAllChildren()));
  }

  public @NotNull Stream<CelTag> getDependencies() {
    return dependencies.stream();
  }

  public @NotNull Optional<String> getPrettyName(String lang) {
    return prettyNameForLangGetter.apply(lang);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, name, scope);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof CelTag) {
      CelTag other = (CelTag) obj;
      return Objects.equals(this.type, other.type)
          && Objects.equals(this.name, other.name)
          && Objects.equals(this.scope, other.scope);
    }
    return false;
  }

  @Override
  public String toString() {
    return "CelTag"
        + " [type=" + type
        + ", name=" + name
        + ", scope=" + scope
        + ", parent=" + parent
        + ", dependencies=" + dependencies
        + "]";
  }

  public static class Builder implements Comparable<Builder> {

    private String type = "";
    private String name = "";
    private EntityReference scope;
    private String parent = "";
    private final Set<String> dependencyNames = new LinkedHashSet<>();
    private final Map<String, CelTag> dependencies = new LinkedHashMap<>();
    private Function<String, Optional<String>> prettyNameForLangGetter;
    private int order;
    private Object source; // only for logging in case of error

    public Builder type(String type) {
      this.type = Strings.nullToEmpty(type).trim().toLowerCase();
      return this;
    }

    public Builder name(String name) {
      this.name = Strings.nullToEmpty(name).trim().toLowerCase();
      return this;
    }

    public Builder scope(EntityReference scope) {
      this.scope = scope;
      return this;
    }

    public Builder parent(String parent) {
      this.parent = Strings.nullToEmpty(parent).trim().toLowerCase();
      if (!this.parent.isEmpty()) {
        expectDependency(this.parent);
      }
      return this;
    }

    public void expectDependency(String tag) {
      dependencyNames.add(Strings.nullToEmpty(tag).trim().toLowerCase());
    }

    public void addDependency(CelTag tag) {
      if ((tag != null) && dependencyNames.remove(tag.getName())) {
        dependencies.put(tag.getName(), tag);
      }
    }

    public boolean hasAllDependencies() {
      return dependencyNames.isEmpty();
    }

    public Builder prettyName(Function<String, Optional<String>> prettyNameForLangGetter) {
      this.prettyNameForLangGetter = prettyNameForLangGetter;
      return this;
    }

    public Builder order(int order) {
      this.order = order;
      return this;
    }

    public Builder source(Object source) {
      this.source = source;
      return this;
    }

    public CelTag build() {
      return new CelTag(this);
    }

    @Override
    public int compareTo(Builder other) {
      return Integer.compare(this.order, other.order);
    }

    @Override
    public String toString() {
      return "CelTag.Builder"
          + " [type=" + type
          + ", name=" + name
          + ", scope=" + scope
          + ", parent=" + parent
          + ", dependencies=" + dependencyNames
          + ", order=" + order
          + ", source=" + source
          + "]";
    }

  }

}
