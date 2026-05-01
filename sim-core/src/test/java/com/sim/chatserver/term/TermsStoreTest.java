package com.sim.chatserver.term;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sim.chatserver.startup.AppDataSourceHolder;

class TermsStoreTest {

    private TermsStore underTest;

    private AppDataSourceHolder dsHolder;
    private DataSource ds;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        underTest = new TermsStore();

        dsHolder = mock(AppDataSourceHolder.class);
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        underTest.setDataSourceHolder(dsHolder);
    }

    @Test
    void ensureTable_postgres_executesAndUpsertsDefault() throws Exception {
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(ps.execute()).thenReturn(true);
        when(ps.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> underTest.ensureTable());

        verify(conn, atLeastOnce()).prepareStatement(contains("CREATE TABLE IF NOT EXISTS term_definition"));
        verify(ps, atLeastOnce()).execute();
    }

    @Test
    void listAll_noRows_returnsEmpty() throws Exception {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<TermDefinition> out = underTest.listAll();

        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void listAll_twoRows_returnsTwo() throws Exception {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);

        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("name")).thenReturn("A", "B");
        when(rs.getString("description")).thenReturn("DA", "DB");
        when(rs.getString("match_pattern")).thenReturn("PA", "PB");
        when(rs.getString("match_type")).thenReturn("WILDCARD", "REGEX");
        when(rs.getBoolean("system_flag")).thenReturn(false, true);

        List<TermDefinition> out = underTest.listAll();

        assertEquals(2, out.size());
    }

    @Test
    void createTerm_returningPath_returnsCreatedTerm() throws Exception {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(42L);

        TermDefinition out = underTest.createTerm("name", "desc", "pat", "wildcard");

        assertNotNull(out);
        assertEquals(42L, out.getId());
        assertEquals("name", out.getName());
    }

    @Test
    void createTerm_fallbackPath_returnsCreatedTerm() throws Exception {
        PreparedStatement insertReturning = mock(PreparedStatement.class);
        PreparedStatement insertPlain = mock(PreparedStatement.class);
        PreparedStatement selectByName = mock(PreparedStatement.class);
        ResultSet selectRs = mock(ResultSet.class);

        when(conn.prepareStatement(contains("RETURNING id"))).thenReturn(insertReturning);
        when(conn.prepareStatement(eq("INSERT INTO term_definition (name, description, match_pattern, match_type, system_flag) VALUES (?, ?, ?, ?, FALSE)")))
                .thenReturn(insertPlain);
        when(conn.prepareStatement(eq("SELECT id FROM term_definition WHERE name = ?"))).thenReturn(selectByName);

        when(insertReturning.executeQuery()).thenThrow(new SQLException("no RETURNING support"));
        when(selectByName.executeQuery()).thenReturn(selectRs);
        when(selectRs.next()).thenReturn(true);
        when(selectRs.getLong(1)).thenReturn(5L);

        TermDefinition out = underTest.createTerm("n", "d", "p", "wildcard");

        assertNotNull(out);
        assertEquals(5L, out.getId());
    }

    @Test
    void isSystemTerm_true_whenRowTrue() throws Exception {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getBoolean("system_flag")).thenReturn(true);

        assertTrue(underTest.isSystemTerm(1L));
    }

    @Test
    void deleteTerm_systemTerm_returnsFalse() throws Exception {
        PreparedStatement checkPs = mock(PreparedStatement.class);
        PreparedStatement deletePs = mock(PreparedStatement.class);
        ResultSet checkRs = mock(ResultSet.class);

        when(conn.prepareStatement(eq("SELECT system_flag FROM term_definition WHERE id = ?"))).thenReturn(checkPs);
        when(conn.prepareStatement(eq("DELETE FROM term_definition WHERE id = ?"))).thenReturn(deletePs);

        when(checkPs.executeQuery()).thenReturn(checkRs);
        when(checkRs.next()).thenReturn(true);
        when(checkRs.getBoolean("system_flag")).thenReturn(true);

        boolean out = underTest.deleteTerm(9L);

        assertFalse(out);
        verify(deletePs, never()).executeUpdate();
    }

    @Test
    void deleteTerm_nonSystem_deletes() throws Exception {
        PreparedStatement checkPs = mock(PreparedStatement.class);
        PreparedStatement deletePs = mock(PreparedStatement.class);
        ResultSet checkRs = mock(ResultSet.class);

        when(conn.prepareStatement(eq("SELECT system_flag FROM term_definition WHERE id = ?"))).thenReturn(checkPs);
        when(conn.prepareStatement(eq("DELETE FROM term_definition WHERE id = ?"))).thenReturn(deletePs);

        when(checkPs.executeQuery()).thenReturn(checkRs);
        when(checkRs.next()).thenReturn(true);
        when(checkRs.getBoolean("system_flag")).thenReturn(false);
        when(deletePs.executeUpdate()).thenReturn(1);

        boolean out = underTest.deleteTerm(9L);

        assertTrue(out);
    }

    @Test
    void updateTerm_systemTerm_returnsNull() throws Exception {
        PreparedStatement checkPs = mock(PreparedStatement.class);
        ResultSet checkRs = mock(ResultSet.class);

        when(conn.prepareStatement(eq("SELECT system_flag FROM term_definition WHERE id = ?"))).thenReturn(checkPs);
        when(checkPs.executeQuery()).thenReturn(checkRs);
        when(checkRs.next()).thenReturn(true);
        when(checkRs.getBoolean("system_flag")).thenReturn(true);

        TermDefinition out = underTest.updateTerm(1L, "n", "d", "p", "t");

        assertNull(out);
    }

    @Test
    void findFirstMatchingTermForPrompt_nullOrEmpty_returnsNull() throws Exception {
        assertNull(underTest.findFirstMatchingTermForPrompt(null));
        assertNull(underTest.findFirstMatchingTermForPrompt(""));
    }

    @Test
    void findFirstMatchingTermInPrompts_usesSingleListAllReadAndFindsMatch() throws Exception {
        PreparedStatement listPs = mock(PreparedStatement.class);
        ResultSet listRs = mock(ResultSet.class);

        when(conn.prepareStatement(contains("SELECT id, name, description, match_pattern, match_type, system_flag FROM term_definition")))
                .thenReturn(listPs);
        when(listPs.executeQuery()).thenReturn(listRs);

        when(listRs.next()).thenReturn(true, false);
        when(listRs.getLong("id")).thenReturn(1L);
        when(listRs.getString("name")).thenReturn("termA");
        when(listRs.getString("description")).thenReturn("desc");
        when(listRs.getString("match_pattern")).thenReturn("*hello*");
        when(listRs.getString("match_type")).thenReturn("WILDCARD");
        when(listRs.getBoolean("system_flag")).thenReturn(false);

        TermDefinition out = underTest.findFirstMatchingTermInPrompts(Arrays.asList("hello world", "other"));

        assertNotNull(out);
        assertEquals("termA", out.getName());

        // important perf assertion for updated class: listAll only once for all prompts
        verify(ds, times(1)).getConnection();
    }

    @Test
    void findFirstMatchingTermInPrompts_nullOrEmptyIterable_returnsNull() throws Exception {
        assertNull(underTest.findFirstMatchingTermInPrompts(null));
        assertNull(underTest.findFirstMatchingTermInPrompts(Collections.emptyList()));
    }
}
