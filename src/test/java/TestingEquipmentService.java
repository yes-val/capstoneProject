import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.EquipmentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestingEquipmentService {
    private static final String EQUIPMENT_NOT_FOUND = "Equipment not found";

    @Mock
    private EquipmentDao equipmentDao;

    @Captor
    private ArgumentCaptor<Equipment> equipmentCaptor; //clean code, put it into

    @InjectMocks
    private EquipmentService equipmentService; // testingInstance []

    //First has to be overriden public methods, then public methods, then protected methods, then default method, then private (if one PM calls another PM, the called one follows below)

    private static final int EQUIPMENT_ID = 10;


    // ---------------------------------------------------------------
    // getActiveEquipment
    // ---------------------------------------------------------------

    @Test
    void getActiveEquipment_returnsListFromDao() {
        // GIVEN
        List<Equipment> expected = List.of(getEquipment(1, true), getEquipment(2, true));
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
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(getEquipment(EQUIPMENT_ID, true)));

        // WHEN
        Equipment result = equipmentService.getById(EQUIPMENT_ID);

        // THEN
        verify(equipmentDao).findById(EQUIPMENT_ID);
        assertEquals(EQUIPMENT_ID, result.getEquipmentId());
    }

    @Test
    void shouldNotGetByIdWhenEquipmentNotFound() { //camelCaseOnly
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.empty());

        // WHEN
        Executable executable = () -> equipmentService.getById(EQUIPMENT_ID);

        // THEN
        assertThrows(BookingException.class, executable, EQUIPMENT_NOT_FOUND);
    }

    // ---------------------------------------------------------------
    // createEquipment
    // ---------------------------------------------------------------

    @Test
    void createEquipment_setsActiveTrueAndSaves() {
        // GIVEN
        Equipment newEquipment = getEquipment(0, false);

        // WHEN
        equipmentService.createEquipment(newEquipment);

        // THEN
        verify(equipmentDao).save(equipmentCaptor.capture());
        assertTrue(equipmentCaptor.getValue().isActive());
    }

    @Test
    void createEquipment_forcesActiveTrue_evenWhenAlreadyActive() {
        // GIVEN
        Equipment newEquipment = getEquipment(0, true);

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
        Equipment existing = getEquipment(EQUIPMENT_ID, false);

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
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(getEquipment(EQUIPMENT_ID, true)));

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
        assertEquals(EQUIPMENT_NOT_FOUND, exception.getMessage());
    }

    private Equipment getEquipment(int equipmentId, boolean active) {
        Equipment equipment = new Equipment();
        equipment.setEquipmentId(equipmentId);
        equipment.setActive(active);

        return equipment;
    }
}
