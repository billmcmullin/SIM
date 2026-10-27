package com.sim.chatserver.web.dashboard.drilldown;

import com.sim.chatserver.service.WorkflowRuntime;
import com.sim.chatserver.startup.AppDataSourceHolder;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public final class WorkflowLifecycleListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        WorkflowRuntime.initialize(CDI.current().select(AppDataSourceHolder.class).get().getDataSource());
        WidgetReviewJobStatusServlet.jobService().enablePersistence(WorkflowRuntime.records());
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        WidgetReviewJobStatusServlet.jobService().close();
        WorkflowRuntime.shutdown();
    }
}