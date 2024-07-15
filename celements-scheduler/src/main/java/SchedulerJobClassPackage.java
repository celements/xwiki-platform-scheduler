import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;

import com.celements.model.classes.AbstractClassPackage;
import com.celements.model.classes.ClassDefinition;
import com.celements.scheduler.classdefs.SchedulerJobClassRole;

@Component
public class SchedulerJobClassPackage extends AbstractClassPackage {

  public static final String NAME = "scheduler";

  private final List<SchedulerJobClassRole> classDefs;

  @Inject
  public SchedulerJobClassPackage(ListableBeanFactory beanFactory) {
    this.classDefs = List.copyOf(beanFactory.getBeansOfType(SchedulerJobClassRole.class).values());
  }

  @Override
  public @NotEmpty String getName() {
    return NAME;
  }

  @Override
  public @NotNull List<? extends ClassDefinition> getClassDefinitions() {
    return classDefs;
  }

}
