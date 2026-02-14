package com.famistry.famistry_personnel.service;

import com.famistry.famistry_personnel.model.Person;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersonServiceMetadataTest {

    @Test
    void testCreateMetadataFromObject() throws Exception {
        // Create a test person with various field types
        Person person = new Person();
        person.setId("123");
        person.setName("John Doe");
        person.setGender("male");
        person.setBirthDate(LocalDate.of(1990, 1, 1));
        person.setDeathDate(null); // This should be excluded
        person.setFatherId("456");
        person.setMotherId("789");
        person.setSpouseId(null); // This should be excluded
        person.setAlive(true);
        person.setImageUrl("http://example.com/image.jpg");
        person.setComment("Test comment");

        // Use reflection to access the private method
        PersonService service = new PersonService(null, null);
        Method method = PersonService.class.getDeclaredMethod("createMetadataFromObject", Object.class);
        method.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) method.invoke(service, person);

        // Verify the metadata contains expected fields
        assertNotNull(metadata);
        assertEquals("123", metadata.get("id"));
        assertEquals("John Doe", metadata.get("name"));
        assertEquals("male", metadata.get("gender"));
        assertEquals("1990-01-01", metadata.get("birthDate")); // LocalDate converted to string
        assertEquals("456", metadata.get("fatherId"));
        assertEquals("789", metadata.get("motherId"));
        assertEquals(true, metadata.get("alive"));
        assertEquals("http://example.com/image.jpg", metadata.get("imageUrl"));
        assertEquals("Test comment", metadata.get("comment"));

        // Verify null values are excluded
        assertFalse(metadata.containsKey("deathDate"));
        assertFalse(metadata.containsKey("spouseId"));

        // Verify complex objects are excluded
        assertFalse(metadata.containsKey("attributes"));
        assertFalse(metadata.containsKey("relationships"));
    }

    @Test
    void testCreateMetadataFromNullObject() throws Exception {
        PersonService service = new PersonService(null, null);
        Method method = PersonService.class.getDeclaredMethod("createMetadataFromObject", Object.class);
        method.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) method.invoke(service, (Object) null);

        assertNotNull(metadata);
        assertTrue(metadata.isEmpty());
    }
}
