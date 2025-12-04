package org.example;


import javax.swing.*;
import java.awt.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

class VentanaPrincipal extends JFrame {

    private static final Color AZUL = new Color(0, 82, 158);
    private static final Color DORADO = new Color(248, 187, 0);

    private JPanel panelPrincipal;
    private CardLayout cardLayout;

    private DataSource dataSource;
    private ClienteSocket clienteSocket;
    private boolean leyendo;
    private ChartPanel chartMonitor, chartHistorico;
    private DefaultCategoryDataset datasetMonitor;
    private int contador;
    private JComboBox<String> comboPuertos;
    private JButton btnIniciarDetener;

    // VARIABLES PARA LOS NUEVOS FILTROS
    private JSpinner spinnerFecha;
    private JSpinner spinnerHora;

    public VentanaPrincipal() {
        clienteSocket = new ClienteSocket();
        setTitle("Sistema de Monitoreo - UNISON");

        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));

        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        panelPrincipal = new JPanel(cardLayout);

        panelPrincipal.add(crearInicio(), "inicio");
        panelPrincipal.add(crearMonitor(), "monitor");
        panelPrincipal.add(crearHistorico(), "historico");

        add(panelPrincipal);
        setVisible(true);
    }

    private JButton crearBotonBarra(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return b;
    }

    private JPanel crearInicio() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        try {
            ImageIcon icon = new ImageIcon("escudo.gif");

            Image imagen = icon.getImage();
            Image nuevaImagen = imagen.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            ImageIcon nuevoIcono = new ImageIcon(nuevaImagen);

            JLabel logo = new JLabel(nuevoIcono);
            gbc.gridy = 0;
            p.add(logo, gbc);

        } catch (Exception e) {
            JLabel logo = new JLabel("LOGO UNISON (Error al cargar)");
            logo.setFont(new Font("Segoe UI", Font.BOLD, 48));
            logo.setForeground(AZUL);
            gbc.gridy = 0;
            p.add(logo, gbc);
            System.err.println("Advertencia: No se pudo cargar 'escudo.gif'. Usando placeholder.");
        }

        JLabel titulo = new JLabel("Sistema de Monitoreo");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titulo.setForeground(AZUL);
        gbc.gridy = 1;
        p.add(titulo, gbc);

        JLabel autor = new JLabel("Autor: Diego Alejandro Velasquez Gonzalez");
        autor.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        autor.setForeground(Color.GRAY);
        gbc.gridy = 2;
        p.add(autor, gbc);

        JButton btnMonitor = crearBotonBarra("Monitor", AZUL);
        btnMonitor.addActionListener(e -> cardLayout.show(panelPrincipal, "monitor"));

        JButton btnHistorico = crearBotonBarra("Histórico", DORADO);
        btnHistorico.addActionListener(e -> {
            cardLayout.show(panelPrincipal, "historico");
            cargarHistorico();
        });

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.add(btnMonitor);
        panelBotones.add(btnHistorico);

        gbc.gridy = 3;
        p.add(panelBotones, gbc);

        return p;
    }

    private JPanel crearMonitor() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.setBackground(AZUL);

        JButton volver = crearBotonBarra("Inicio", AZUL);
        volver.addActionListener(e -> {
            if (leyendo) detener();
            cardLayout.show(panelPrincipal, "inicio");
        });

        String[] puertos = SerialReader.listarPuertos();
        String[] nombresPuertos;

        if (puertos.length == 0) {
            nombresPuertos = new String[]{"SIMULADOR"};
        } else {
            nombresPuertos = new String[puertos.length + 1];
            nombresPuertos[0] = "SIMULADOR";
            System.arraycopy(puertos, 0, nombresPuertos, 1, puertos.length);
        }

        comboPuertos = new JComboBox<>(nombresPuertos);
        comboPuertos.setSelectedItem("SIMULADOR");

        btnIniciarDetener = crearBotonBarra("Iniciar", DORADO);
        btnIniciarDetener.addActionListener(e -> toggle());

        barra.add(volver);
        barra.add(new JLabel("Puerto: "));
        barra.add(comboPuertos);
        barra.add(btnIniciarDetener);
        p.add(barra, BorderLayout.NORTH);

        datasetMonitor = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createLineChart(
                "Monitoreo en Tiempo Real", "Tiempo (segundos)", "Valor (0-100)",
                datasetMonitor, PlotOrientation.VERTICAL, true, true, false);
        chartMonitor = new ChartPanel(chart);
        p.add(chartMonitor, BorderLayout.CENTER);

        return p;
    }

    private JPanel crearHistorico() {

        JPanel p = new JPanel(new BorderLayout());
        JPanel barraSuperior = new JPanel(new BorderLayout());
        JPanel barraBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraBotones.setBackground(AZUL);

        JButton volver = crearBotonBarra("Inicio", AZUL);
        volver.addActionListener(e -> cardLayout.show(panelPrincipal, "inicio"));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelFiltros.setBackground(DORADO);
        panelFiltros.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        spinnerFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerFecha, "yyyy-MM-dd");
        spinnerFecha.setEditor(dateEditor);
        spinnerFecha.setValue(new Date());

        panelFiltros.add(new JLabel("Fecha: "));
        panelFiltros.add(spinnerFecha);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date horaInicial = cal.getTime();

        spinnerHora = new JSpinner(new SpinnerDateModel(horaInicial, null, null, Calendar.HOUR_OF_DAY));
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinnerHora, "HH:mm");
        spinnerHora.setEditor(timeEditor);

        panelFiltros.add(new JLabel("Hora (Desde): "));
        panelFiltros.add(spinnerHora);


        JButton btnCargar = crearBotonBarra("Recargar Histórico", DORADO);
        btnCargar.addActionListener(e -> cargarHistorico());

        barraBotones.add(volver);
        barraBotones.add(btnCargar);

        barraSuperior.add(barraBotones, BorderLayout.NORTH);
        barraSuperior.add(panelFiltros, BorderLayout.CENTER);

        p.add(barraSuperior, BorderLayout.NORTH);

        chartHistorico = new ChartPanel(createEmptyChart("Cargue datos para ver el historial"));
        p.add(chartHistorico, BorderLayout.CENTER);

        return p;
    }

    private JFreeChart createEmptyChart(String titulo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        return ChartFactory.createLineChart(
                titulo, "Fecha y Hora", "Valor",
                dataset, PlotOrientation.VERTICAL, true, true, false);
    }

    private void toggle() {
        if (!leyendo) iniciar();
        else detener();
    }

    private void iniciar() {
        String puertoSeleccionado = (String) comboPuertos.getSelectedItem();

        SerialReader.DatosListener listener = (x, y, z) -> {
            SwingUtilities.invokeLater(() -> {
                contador++;
                datasetMonitor.addValue(x, "X", String.valueOf(contador));
                datasetMonitor.addValue(y, "Y", String.valueOf(contador));
                datasetMonitor.addValue(z, "Z", String.valueOf(contador));
                if (contador > 50) datasetMonitor.removeColumn(0);
            });
            clienteSocket.enviarDatos(x, y, z);
        };

        DataSource intentoFuente;

        if (puertoSeleccionado.equals("SIMULADOR")) {
            intentoFuente = new SimuladorArduino(listener);
        }
        else {
            intentoFuente = new SerialReader(puertoSeleccionado, listener);
        }

        boolean inicioExitoso = intentoFuente.iniciar();

        if (inicioExitoso) {
            this.dataSource = intentoFuente;
            leyendo = true;
            btnIniciarDetener.setText("Detener");
        } else {
            leyendo = false;
            btnIniciarDetener.setText("Iniciar");
            this.dataSource = null;

            JOptionPane.showMessageDialog(this,
                    "El puerto '" + puertoSeleccionado + "' no pudo ser abierto (Puerto ocupado, o dispositivo desconectado).\n\n" +
                            "Por favor, seleccione 'SIMULADOR'.",
                    "Error de Conexión Serial", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void detener() {
        if (dataSource != null) {
            dataSource.detener();
        }
        leyendo = false;
        btnIniciarDetener.setText("Iniciar");
    }

    private void cargarHistorico() {
        chartHistorico.setChart(createEmptyChart("Cargando datos históricos... Por favor espere."));


        SimpleDateFormat formatFecha = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm");

        String fechaFiltro = formatFecha.format((Date) spinnerFecha.getValue());
        String horaFiltro = formatHora.format((Date) spinnerHora.getValue());

        new SwingWorker<List<DatoSensor>, Void>() {

            @Override
            protected List<DatoSensor> doInBackground() throws Exception {

                return clienteSocket.consultarDatos(fechaFiltro, horaFiltro);
            }

            @Override
            protected void done() {
                try {
                    List<DatoSensor> datos = get();

                    if (datos.isEmpty()) {
                        chartHistorico.setChart(createEmptyChart("No se encontraron datos históricos con esos filtros."));
                        return;
                    }

                    DefaultCategoryDataset datasetHistorico = new DefaultCategoryDataset();

                    for (DatoSensor d : datos) {
                        String etiqueta = d.fecha + " " + d.hora;

                        datasetHistorico.addValue(d.x, "Eje X", etiqueta);
                        datasetHistorico.addValue(d.y, "Eje Y", etiqueta);
                        datasetHistorico.addValue(d.z, "Eje Z", etiqueta);
                    }

                    JFreeChart chart = ChartFactory.createLineChart(
                            "Datos Históricos Cargados (" + datos.size() + " registros)",
                            "Fecha y Hora de Captura", "Valor (0-100)",
                            datasetHistorico, PlotOrientation.VERTICAL, true, true, false);

                    chartHistorico.setChart(chart);

                } catch (Exception e) {
                    chartHistorico.setChart(createEmptyChart("Error al cargar datos."));
                    JOptionPane.showMessageDialog(VentanaPrincipal.this,
                            "Ocurrió un error al cargar el historial: " + e.getMessage(),
                            "Error de Carga", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(VentanaPrincipal::new);
    }
}
