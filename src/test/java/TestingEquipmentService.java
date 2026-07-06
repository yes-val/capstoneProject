import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.impl.EquipmentServiceImpl;

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
    private ArgumentCaptor<Equipment> equipmentCaptor;

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    private static final int EQUIPMENT_ID = 10;

    @Test
    void shouldReturnActiveEquipmentListFromDao() {
        List<Equipment> expected = List.of(getEquipment(1, true), getEquipment(2, true));
        when(equipmentDao.findAllActive()).thenReturn(expected);

        List<Equipment> result = equipmentService.getActiveEquipment();

        verify(equipmentDao).findAllActive();
        assertEquals(expected, result);
    }

    @Test
    void shouldReturnEmptyListWhenNoEquipmentActive() {
        when(equipmentDao.findAllActive()).thenReturn(List.of());

        List<Equipment> result = equipmentService.getActiveEquipment();

        verify(equipmentDao).findAllActive();
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEquipmentWhenFoundById() {
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(getEquipment(EQUIPMENT_ID, true)));

        Equipment result = equipmentService.getById(EQUIPMENT_ID);

        verify(equipmentDao).findById(EQUIPMENT_ID);
        assertEquals(EQUIPMENT_ID, result.getEquipmentId());
    }

    @Test
    void shouldThrowExceptionWhenGettingEquipmentByIdNotFound() {
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.empty());

        Executable executable = () -> equipmentService.getById(EQUIPMENT_ID);

        assertThrows(BookingException.class, executable, EQUIPMENT_NOT_FOUND);
    }

    @Test
    void shouldSetActiveTrueAndSaveWhenCreatingEquipment() {
        Equipment newEquipment = getEquipment(0, false);

        equipmentService.createEquipment(newEquipment);

        verify(equipmentDao).save(equipmentCaptor.capture());
        assertTrue(equipmentCaptor.getValue().isActive());
    }

    @Test
    void shouldForceActiveTrueWhenCreatingAlreadyActiveEquipment() {
        Equipment newEquipment = getEquipment(0, true);

        equipmentService.createEquipment(newEquipment);

        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentDao).save(captor.capture());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void shouldSaveEquipmentAsIsWhenUpdating() {
        Equipment existing = getEquipment(EQUIPMENT_ID, false);

        equipmentService.updateEquipment(existing);

        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentDao).save(captor.capture());
        assertFalse(captor.getValue().isActive());
        assertEquals(EQUIPMENT_ID, captor.getValue().getEquipmentId());
    }

    @Test
    void shouldDeactivateEquipmentWhenFound() {
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(getEquipment(EQUIPMENT_ID, true)));

        equipmentService.deactivateEquipment(EQUIPMENT_ID);

        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentDao).save(captor.capture());
        assertFalse(captor.getValue().isActive());
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingEquipmentNotFound() {
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.empty());

        BookingException exception = assertThrows(BookingException.class,
                () -> equipmentService.deactivateEquipment(EQUIPMENT_ID));

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
