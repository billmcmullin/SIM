package com.sim.chatserver.term;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

/**
 * Unit tests for TermsStore using in-memory H2.
 */
class TermsStoreTest {

    private TermsStore store;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:termsstore;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");

        this.dataSource = ds;

        AppDataSourceHolder holder = new AppDataSourceHolder() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        // Option A: use no-arg constructor + setter injection
        this.store = new TermsStore();
        this.store.setDataSourceHolder(holder);
    }

    @Test
    void ensureTable_createsTable_andDefaultSystemTerm() throws Exception {
        store.ensureTable();

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM term_definition")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 1, "Expected at least default term row");
            }
        }

        List<TermDefinition> terms = store.listAll();
        assertFalse(terms.isEmpty(), "Expected terms not empty after ensureTable");

        boolean foundDefault = terms.stream().anyMatch(t -> "Other Parasoft Match".equals(t.getName()));
        assertTrue(foundDefault, "Expected default system term to exist");
    }

    @Test
    void createTerm_persistsAndReturnsTerm() throws Exception {
        store.ensureTable();

        TermDefinition created = store.createTerm(
                "Jtest",
                "Jtest related inquiry",
                "jtest*",
                "WILDCARD"
        );

        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals("Jtest", created.getName());
        assertEquals("Jtest related inquiry", created.getDescription());
        assertEquals("jtest*", created.getMatchPattern());
        assertEquals("WILDCARD", created.getMatchType());
        assertFalse(created.isSystemFlag());

        List<TermDefinition> all = store.listAll();
        assertTrue(all.stream().anyMatch(t -> "Jtest".equals(t.getName())));
    }

    @Test
    void updateTerm_updatesNonSystemTerm() throws Exception {
        store.ensureTable();

        TermDefinition created = store.createTerm(
                "SOAtest",
                "old description",
                "soa*",
                "WILDCARD"
        );
        assertNotNull(created);

        TermDefinition updated = store.updateTerm(
                created.getId(),
                "SOAtest Updated",
                "new description",
                "soatest*",
                "WILDCARD"
        );

        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals("SOAtest Updated", updated.getName());
        assertEquals("new description", updated.getDescription());
        assertEquals("soatest*", updated.getMatchPattern());
    }

    @Test
    void deleteTerm_deletesNonSystemTerm() throws Exception {
        store.ensureTable();

        TermDefinition created = store.createTerm(
                "Virtualize",
                "virtualize term",
                "virt*",
                "WILDCARD"
        );
        assertNotNull(created);

        boolean deleted = store.deleteTerm(created.getId());
        assertTrue(deleted);

        List<TermDefinition> all = store.listAll();
        assertFalse(all.stream().anyMatch(t -> t.getId().equals(created.getId())));
    }

    @Test
    void deleteTerm_doesNotDeleteSystemTerm() throws Exception {
        store.ensureTable();

        TermDefinition system = store.listAll().stream()
                .filter(TermDefinition::isSystemFlag)
                .findFirst()
                .orElseThrow();

        boolean deleted = store.deleteTerm(system.getId());
        assertFalse(deleted);

        List<TermDefinition> all = store.listAll();
        assertTrue(all.stream().anyMatch(t -> t.getId().equals(system.getId())));
    }

    @Test
    void findFirstMatchingTermForPrompt_returnsLeftmostMatch() throws Exception {
        store.ensureTable();

        store.createTerm("TermA", "A", "alpha*", "WILDCARD");
        store.createTerm("TermB", "B", "beta*", "WILDCARD");

        TermDefinition found = store.findFirstMatchingTermForPrompt("beta-one then alpha-one");
        assertNotNull(found);
        assertEquals("TermB", found.getName(), "Expected leftmost match in text");
    }

    @Test
    void findFirstMatchingTermInPrompts_returnsFirstPromptMatchInOrder() throws Exception {
        store.ensureTable();

        store.createTerm("Docs", "docs", "docs*", "WILDCARD");
        store.createTerm("API", "api", "api*", "WILDCARD");

        List<String> prompts = List.of(
                "no match here",
                "api-security topic",
                "docs-reference later"
        );

        TermDefinition found = store.findFirstMatchingTermInPrompts(prompts);
        assertNotNull(found);
        assertEquals("API", found.getName(), "Expected first prompt in order with a match");
    }

    @Test
    void findFirstMatchingTermForPrompt_returnsNullForBlank() throws Exception {
        store.ensureTable();

        assertNull(store.findFirstMatchingTermForPrompt(null));
        assertNull(store.findFirstMatchingTermForPrompt(""));
        assertNull(store.findFirstMatchingTermForPrompt("   "));
    }
}
