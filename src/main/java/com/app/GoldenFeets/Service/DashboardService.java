package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.DTO.DashboardDTO;
import com.app.GoldenFeets.Repository.PedidoDetalleRepository;
import com.app.GoldenFeets.Repository.PedidoRepository;
import com.app.GoldenFeets.Repository.ProductoVarianteRepository;
import com.app.GoldenFeets.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList; // Importante
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final ProductoVarianteRepository varianteRepository;
    private final PedidoDetalleRepository detalleRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardDTO obtenerDatosDashboard() {
        DashboardDTO data = new DashboardDTO();
        LocalDateTime inicioDia = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime finDia = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime inicioMes = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);

        // 1. Estadísticas Generales
        Double ventasHoy = pedidoRepository.sumarVentasPorRango(inicioDia, finDia);
        data.setVentasHoy(ventasHoy != null ? ventasHoy : 0.0);
        data.setPedidosHoy(pedidoRepository.contarPedidosPorRango(inicioDia, finDia));
        Double ventasMes = pedidoRepository.sumarVentasPorRango(inicioMes, finDia);
        data.setIngresosMes(ventasMes != null ? ventasMes : 0.0);
        data.setUsuariosRegistrados((int) usuarioRepository.count());

        // 2. Notificaciones
        data.setProductosBajoStock(varianteRepository.findByStockLessThanAndActivoTrue(5));

        // 3. Últimos Pedidos
        data.setUltimosPedidos(pedidoRepository.findTop5ByOrderByFechaCreacionDesc());

        // 4. Top Productos (Lógica Simplificada para Gráfico)
        List<Object[]> topList = detalleRepository.encontrarTopProductosVendidos();

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : topList) {
            // row[0] es el nombre (String), row[1] es la cantidad (Long/BigInteger)
            labels.add((String) row[0]);
            values.add(((Number) row[1]).longValue());
        }

        data.setTopLabels(labels);
        data.setTopValues(values);

        return data;
    }
}