package com.xpn.xwiki.plugin.scheduler;

/**
 * This class is only needed for existing DB references to still work after renaming the scheduler
 * package. Use com.celements.scheduler.job.GroovyJob for new work.
 */

@Deprecated
public class GroovyJob extends com.celements.scheduler.job.GroovyJob {

}
