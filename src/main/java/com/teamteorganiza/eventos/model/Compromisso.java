package com.teamteorganiza.eventos.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Um compromisso/evento do CTG (baile, rodeio, ensaio de invernada, etc.).
 *
 * O armazenamento fica a cargo do CompromissoRepository — a entidade só carrega
 * os dados, no mesmo padrão de Pessoa e Produto.
 */
public class Compromisso {

    private static int contadorId = 0;

    private int id;
    private String titulo;
    private TipoCompromisso tipo;
    private String categoria;       // antes "invernada"; vale para qualquer tipo
    private LocalDate data;
    private String horario;
    private String local;
    private String responsavel;
    private String descricao;

    private final List<String> participantes = new ArrayList<>();
    private final List<String> caronas = new ArrayList<>();

    public Compromisso(String titulo, TipoCompromisso tipo, String categoria, LocalDate data,
                       String horario, String local, String responsavel, String descricao) {
        this.id = ++contadorId;
        this.titulo = titulo;
        this.tipo = tipo;
        this.categoria = categoria;
        this.data = data;
        this.horario = horario;
        this.local = local;
        this.responsavel = responsavel;
        this.descricao = descricao;
    }

    public int getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public TipoCompromisso getTipo() { return tipo; }
    public void setTipo(TipoCompromisso tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    // Listas de apoio (ainda sem tela própria) — mantidas para uso futuro.
    public List<String> getParticipantes() { return participantes; }
    public List<String> getCaronas() { return caronas; }

    @Override
    public String toString() {
        String cat = (categoria != null && !categoria.isEmpty()) ? " | Categoria: " + categoria : "";
        return String.format("[%d] %s (%s)%s - Data: %s | Local: %s | Resp: %s",
                id, titulo, tipo, cat, data, local, responsavel);
    }
}
