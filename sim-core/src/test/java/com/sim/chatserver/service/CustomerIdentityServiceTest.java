package com.sim.chatserver.service;

import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.model.CustomerIdentity;
import com.sim.chatserver.model.CustomerIdentitySessionLink;
import com.sim.chatserver.model.CustomerIdentityStore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for CustomerIdentityService
 *
 * @see com.sim.chatserver.service.CustomerIdentityService
 * @author bmcmullin
 */
public class CustomerIdentityServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for resolveOrCreateBySessionId(String)
     *
     * @see com.sim.chatserver.service.CustomerIdentityService#resolveOrCreateBySessionId(String)
     * @author bmcmullin
     */
    @Test
    public void testResolveOrCreateBySessionId() throws Throwable
    {
        // Given
        CustomerIdentityService underTest = new CustomerIdentityService();

        // When
        String sessionId = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.resolveOrCreateBySessionId(sessionId);
        });

    }

    @Test
    void resolveOrCreateBySessionId_rejectsBlankSessionId() {
        CustomerIdentityService underTest = new CustomerIdentityService();

        assertThrows(IllegalArgumentException.class,
                () -> underTest.resolveOrCreateBySessionId("   "));
    }

    @Test
    void resolveOrCreateBySessionId_returnsExistingIdentityWithoutUpsert() throws Exception {
        CustomerIdentityService underTest = new CustomerIdentityService();
        CustomerIdentity existing = new CustomerIdentity();
        existing.setIdentityId(5L);

        try (MockedStatic<CustomerIdentityStore> storeMock = Mockito.mockStatic(CustomerIdentityStore.class)) {
            storeMock.when(() -> CustomerIdentityStore.findBySessionId("sess-1")).thenReturn(existing);

            CustomerIdentity resolved = underTest.resolveOrCreateBySessionId("sess-1");

            assertEquals(existing, resolved);
            storeMock.verify(() -> CustomerIdentityStore.findBySessionId("sess-1"));
            storeMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void listLinkedSessions_delegatesToStore() throws Exception {
        CustomerIdentityService underTest = new CustomerIdentityService();
        CustomerIdentitySessionLink link = new CustomerIdentitySessionLink();

        try (MockedStatic<CustomerIdentityStore> storeMock = Mockito.mockStatic(CustomerIdentityStore.class)) {
            storeMock.when(() -> CustomerIdentityStore.listSessionLinks(101L)).thenReturn(List.of(link));

            List<CustomerIdentitySessionLink> links = underTest.listLinkedSessions(101L);

            assertEquals(1, links.size());
            assertEquals(link, links.get(0));
        }
    }

    @Test
    void serializationGuards_throwNotSerializableException() throws Exception {
        CustomerIdentityService underTest = new CustomerIdentityService();

        Method readObject = CustomerIdentityService.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
        readObject.setAccessible(true);
        InvocationTargetException readEx = assertThrows(InvocationTargetException.class,
                () -> readObject.invoke(underTest, new Object[]{null}));
        assertTrue(readEx.getCause() instanceof NotSerializableException);

        Method writeObject = CustomerIdentityService.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
        writeObject.setAccessible(true);
        InvocationTargetException writeEx = assertThrows(InvocationTargetException.class,
                () -> writeObject.invoke(underTest, new Object[]{null}));
        assertTrue(writeEx.getCause() instanceof NotSerializableException);
    }

}
