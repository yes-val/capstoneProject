import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.EquipmentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestingEquipmentService {

    @Mock
    private EquipmentDao equipmentDao;

    @InjectMocks
    private EquipmentService equipmentService;

    private static final int EQUIPMENT_ID = 10;

    private Equipment equipment(int equipmentId, boolean active) {
        Equipment equipment = new Equipment();
        equipment.setEquipmentId(equipmentId);
        equipment.setActive(active);
        return equipment;
    }

    // ---------------------------------------------------------------
    // getActiveEquipment
    // ---------------------------------------------------------------

    @Test
    void getActiveEquipment_returnsListFromDao() {
        // GIVEN
        List<Equipment> expected = List.of(equipment(1, true), equipment(2, true));
        when(equipmentDao.findAllActive()).thenReturn(expected);

        // WHEN
        List<Equipment> result = equipmentService.getActiveEquipment();

        // THEN
        verify(equipmentDao).findAllActive();
        assertEquals(expected, result);
    }

    @Test
    void getActiveEquipment_returnsEmptyList_whenNoneActive() {
        // GIVEN
        when(equipmentDao.findAllActive()).thenReturn(List.of());

        // WHEN
        List<Equipment> result = equipmentService.getActiveEquipment();

        // THEN
        verify(equipmentDao).findAllActive();
        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------------
    // getById
    // ---------------------------------------------------------------

    @Test
    void getById_returnsEquipment_whenFound() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(EQUIPMENT_ID, true)));

        // WHEN
        Equipment result = equipmentService.getById(EQUIPMENT_ID);

        // THEN
        verify(equipmentDao).findById(EQUIPMENT_ID);
        assertEquals(EQUIPMENT_ID, result.getEquipmentId());
    }

    @Test
    void getById_throwsException_whenNotFound() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.empty());

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> equipmentService.getById(EQUIPMENT_ID));

        // THEN
        verify(equipmentDao).findById(EQUIPMENT_ID);
        assertEquals("Equipment not found", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // createEquipment
    // ---------------------------------------------------------------

    @Test
    void createEquipment_setsActiveTrueAndSaves() {
        // GIVEN
        Equipment newEquipment = equipment(0, false);

        // WHEN
        equipmentService.createEquipment(newEquipment);

        // THEN
        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentDao).save(captor.capture());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void createEquipment_forcesActiveTrue_evenWhenAlreadyActive() {
        // GIVEN
        Equipment newEquipment = equipment(0, true);

        // WHEN
        equipmentService.createEquipment(newEquipment);

        // THEN
        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentDao).save(captor.capture());
        assertTrue(captor.getValue().isActive());
    }

    // ---------------------------------------------------------------
    // updateEquipment
    // ---------------------------------------------------------------

    @Test
    void updateEquipment_savesEquipmentAsIs() {
        // GIVEN
        Equipment existing = equipment(EQUIPMENT_ID, false);

        // WHEN
        equipmentService.updateEquipment(existing);

        // THEN
        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentDao).save(captor.capture());
        assertFalse(captor.getValue().isActive());
        assertEquals(EQUIPMENT_ID, captor.getValue().getEquipmentId());
    }

    // ---------------------------------------------------------------
    // deactivateEquipment
    // ---------------------------------------------------------------

    @Test
    void deactivateEquipment_success_whenFound() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(EQUIPMENT_ID, true)));

        // WHEN
        equipmentService.deactivateEquipment(EQUIPMENT_ID);

        // THEN
        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentDao).save(captor.capture());
        assertFalse(captor.getValue().isActive());
    }

    @Test
    void deactivateEquipment_throwsException_whenNotFound() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.empty());

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> equipmentService.deactivateEquipment(EQUIPMENT_ID));

        // THEN
        verify(equipmentDao, never()).save(any());
        assertEquals("Equipment not found", exception.getMessage());
    }
}
