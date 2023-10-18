package kitchenpos.application;

import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.TableGroupDto;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(ReplaceUnderscores.class)
@SpringBootTest
class TableGroupServiceTest {

    @Autowired
    private TableGroupService tableGroupService;

    @MockBean
    private OrderTableDao orderTableDao;

    @MockBean
    private TableGroupDao tableGroupDao;

    @MockBean
    private OrderDao orderDao;

    @Test
    void 단체_지정을_생성한다() {
        // given
        OrderTable orderTable1 = new OrderTable(1L, null, 2, true);
        OrderTable orderTable2 = new OrderTable(2L, null, 3, true);
        List<OrderTable> orderTables = List.of(orderTable1, orderTable2);

        TableGroupDto tableGroupDto = new TableGroupDto(LocalDateTime.now(), orderTables);
        TableGroup tableGroup = tableGroupDto.toDomain();
        List<Long> orderTableIds = orderTables.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());

        given(orderTableDao.findAllByIdIn(orderTableIds))
                .willReturn(orderTables);
        given(tableGroupDao.save(any()))
                .willReturn(tableGroup);

        // when
        TableGroupDto result = tableGroupService.create(tableGroupDto);

        // then
        assertThat(result.getOrderTables()).containsAll(orderTables);
    }
    
    @Test
    void 비어있지_않은_테이블을_단체_지정하면_예외를_던진다() {
        // given
        OrderTable orderTable1 = new OrderTable(1L, 1L, 2, false);
        OrderTable orderTable2 = new OrderTable(2L, 1L, 3, false);
        List<OrderTable> orderTables = List.of(orderTable1, orderTable2);

        TableGroupDto tableGroupDto = new TableGroupDto(LocalDateTime.now(), orderTables);
        TableGroup tableGroup = tableGroupDto.toDomain();

        List<Long> orderTableIds = orderTables.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());

        given(orderTableDao.findAllByIdIn(orderTableIds))
                .willReturn(orderTables);
        given(tableGroupDao.save(tableGroup))
                .willReturn(tableGroup);

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(tableGroupDto))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미_단체_지정된_테이블을_단체_지정하면_예외를_던진다() {
        // given
        OrderTable orderTable1 = new OrderTable(1L, 1L, 2, true);
        OrderTable orderTable2 = new OrderTable(2L, 1L, 3, true);
        List<OrderTable> orderTables = List.of(orderTable1, orderTable2);

        TableGroupDto tableGroupDto = new TableGroupDto(LocalDateTime.now(), orderTables);
        TableGroup tableGroup = tableGroupDto.toDomain();

        List<Long> orderTableIds = orderTables.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());

        given(orderTableDao.findAllByIdIn(orderTableIds))
                .willReturn(orderTables);
        given(tableGroupDao.save(tableGroup))
                .willReturn(tableGroup);

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(tableGroupDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 단체_지정을_해제한다() {
        // given
        OrderTable orderTable1 = new OrderTable(1L, 1L, 2, true);
        OrderTable orderTable2 = new OrderTable(2L, 1L, 3, true);
        List<OrderTable> orderTables = List.of(orderTable1, orderTable2);

        TableGroupDto tableGroupDto = new TableGroupDto(LocalDateTime.now(), orderTables);
        TableGroup tableGroup = tableGroupDto.toDomain();

        List<Long> orderTableIds = orderTables.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());

        given(orderTableDao.findAllByTableGroupId(tableGroup.getId()))
                .willReturn(orderTables);
        given(orderDao.existsByOrderTableIdInAndOrderStatusIn(orderTableIds, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name())))
                .willReturn(false);

        // when
        tableGroupService.ungroup(tableGroup.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(orderTable1.getTableGroupId()).isNull();
            softly.assertThat(orderTable1.isEmpty()).isFalse();
            softly.assertThat(orderTable2.getTableGroupId()).isNull();
            softly.assertThat(orderTable2.isEmpty()).isFalse();
        });
    }
}
