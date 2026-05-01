package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.widget.WidgetEntry;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardTermService
 *
 * @see com.sim.chatserver.service.dashboard.DashboardTermService
 * @author bmcmullin
 */
public class DashboardTermServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = null; // UTA: configured value
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        terms.add(item);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary2() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        terms.add(item);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary3() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TermDefinition> terms = null; // UTA: configured value
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary4() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary5() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary6() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary7() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        String getCatalogResult = null; // UTA: configured value
        when(conn.getCatalog()).thenReturn(getCatalogResult);

        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult2 = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult2);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(conn.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary8() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item2 = mock(TermDefinition.class);
        terms.add(item2);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary9() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        widgets.add(item2);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary10() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.next()).thenThrow(SQLException.class);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(conn.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        assertThrows(SQLException.class, () -> {
            underTest.buildTermSummary(conn, widgets, terms);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary11() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.getString(nullable(String.class))).thenThrow(SQLException.class);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(conn.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        assertThrows(SQLException.class, () -> {
            underTest.buildTermSummary(conn, widgets, terms);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary12() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(conn.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        assertThrows(SQLException.class, () -> {
            underTest.buildTermSummary(conn, widgets, terms);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary13() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(prepareStatementResult).close();

        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(conn.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        assertThrows(SQLException.class, () -> {
            underTest.buildTermSummary(conn, widgets, terms);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary14() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = null; // UTA: configured value
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        terms.add(item);
        LocalDate rangeStartInclusive = mock(LocalDate.class);
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary15() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        terms.add(item);
        LocalDate rangeStartInclusive = mock(LocalDate.class);
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary16() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TermDefinition> terms = null; // UTA: configured value
        LocalDate rangeStartInclusive = mock(LocalDate.class);
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary17() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        LocalDate rangeStartInclusive = null; // UTA: configured value
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary18() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        LocalDate rangeStartInclusive = mock(LocalDate.class);
        LocalDateTime atStartOfDayResult = mock(LocalDateTime.class);
        when(rangeStartInclusive.atStartOfDay()).thenReturn(atStartOfDayResult);
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        LocalDate plusDaysResult = mock(LocalDate.class);
        LocalDateTime atStartOfDayResult2 = mock(LocalDateTime.class);
        when(plusDaysResult.atStartOfDay()).thenReturn(atStartOfDayResult2);
        when(rangeEndInclusive.plusDays(anyLong())).thenReturn(plusDaysResult);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary19() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        terms.add(item);
        LocalDate rangeStartInclusive = null; // UTA: configured value
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary20() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        LocalDate rangeStartInclusive = null; // UTA: configured value
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary21() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        LocalDate rangeStartInclusive = null; // UTA: configured value
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary22() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(conn.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        LocalDate rangeStartInclusive = null; // UTA: configured value
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary23() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        String getCatalogResult = null; // UTA: configured value
        when(conn.getCatalog()).thenReturn(getCatalogResult);

        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult2 = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult2);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(conn.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        LocalDate rangeStartInclusive = null; // UTA: configured value
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTermSummary(Connection, List, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#buildTermSummary(Connection, List, LocalDate,
     *      LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildTermSummary24() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        widgets.add(item2);
        List<TermDefinition> terms = new ArrayList<TermDefinition>(); // UTA: default value
        LocalDate rangeStartInclusive = null; // UTA: configured value
        LocalDate rangeEndInclusive = mock(LocalDate.class);
        TermSummary result = underTest.buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);

    }

    /**
     * Parasoft Jtest UTA: Test for loadAllTerms()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#loadAllTerms()
     * @author bmcmullin
     */
    @Test
    public void testLoadAllTerms() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStore).listAll();
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        List<TermDefinition> result = underTest.loadAllTerms();

    }

    /**
     * Parasoft Jtest UTA: Test for loadAllTerms()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#loadAllTerms()
     * @author bmcmullin
     */
    @Test
    public void testLoadAllTerms2() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = null; // UTA: configured value
        doReturn(listAllResult).when(termsStore).listAll();
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        List<TermDefinition> result = underTest.loadAllTerms();

    }

    /**
     * Parasoft Jtest UTA: Test for loadAllTerms()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#loadAllTerms()
     * @author bmcmullin
     */
    @Test
    public void testLoadAllTerms3() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        when(termsStore.listAll()).thenThrow(SQLException.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        List<TermDefinition> result = underTest.loadAllTerms();

    }

    /**
     * Parasoft Jtest UTA: Test for toChartJson(TermSummary)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#toChartJson(TermSummary)
     * @author bmcmullin
     */
    @Test
    public void testToChartJson() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        TermSummary summary = null; // UTA: configured value
        String result = underTest.toChartJson(summary);

    }

    /**
     * Parasoft Jtest UTA: Test for toChartJson(TermSummary)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#toChartJson(TermSummary)
     * @author bmcmullin
     */
    @Test
    public void testToChartJson2() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        TermSummary summary = mock(TermSummary.class);
        Map<String, Integer> getTermCountsResult = new HashMap<String, Integer>(); // UTA: default value
        doReturn(getTermCountsResult).when(summary).getTermCounts();
        String result = underTest.toChartJson(summary);

    }

    /**
     * Parasoft Jtest UTA: Test for toChartJson(TermSummary)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#toChartJson(TermSummary)
     * @author bmcmullin
     */
    @Test
    public void testToChartJson3() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        TermSummary summary = mock(TermSummary.class);
        Map<String, Integer> getTermCountsResult = new HashMap<String, Integer>(); // UTA: default value
        String key = "key"; // UTA: default value
        Integer value = 1; // UTA: default value
        getTermCountsResult.put(key, value);
        Map<String, Integer> getTermCountsResult2 = new HashMap<String, Integer>(); // UTA: default value
        String key2 = "key2"; // UTA: default value
        Integer value2 = 1; // UTA: default value
        getTermCountsResult2.put(key2, value2);
        doReturn(getTermCountsResult, getTermCountsResult2).when(summary).getTermCounts();
        String result = underTest.toChartJson(summary);

    }

    /**
     * Parasoft Jtest UTA: Test for toChartJson(TermSummary)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardTermService#toChartJson(TermSummary)
     * @author bmcmullin
     */
    @Test
    public void testToChartJson4() throws Throwable
    {
        // Given
        TermsStore termsStore = mock(TermsStore.class);
        DashboardTermService underTest = new DashboardTermService(termsStore);

        // When
        TermSummary summary = mock(TermSummary.class);
        Map<String, Integer> getTermCountsResult = new HashMap<String, Integer>(); // UTA: default value
        String key = "key"; // UTA: default value
        Integer value = 1; // UTA: default value
        getTermCountsResult.put(key, value);
        Map<String, Integer> getTermCountsResult2 = new HashMap<String, Integer>(); // UTA: default value
        String key2 = "key2"; // UTA: default value
        Integer value2 = 1; // UTA: default value
        getTermCountsResult2.put(key2, value2);
        String key3 = "key3"; // UTA: default value
        Integer value3 = 1; // UTA: default value
        getTermCountsResult2.put(key3, value3);
        doReturn(getTermCountsResult, getTermCountsResult2).when(summary).getTermCounts();
        String result = underTest.toChartJson(summary);

    }
}
