package com.teamteorganiza.auth;

public class SessaoAtual {

    private static final SessaoAtual INSTANCIA = new SessaoAtual();

    private String usuarioId;
    private String orgId;
    private String nomeOrg;
    private String nome;
    private String perfil;
    private boolean logado = false;

    private SessaoAtual() {}

    public static SessaoAtual get() { return INSTANCIA; }

    public void iniciar(String usuarioId, String orgId, String nomeOrg, String nome, String perfil) {
        this.usuarioId = usuarioId;
        this.orgId     = orgId;
        this.nomeOrg   = nomeOrg;
        this.nome      = nome;
        this.perfil    = perfil;
        this.logado    = true;
    }

    public boolean isLogado()    { return logado; }
    public String  getUsuarioId(){ return usuarioId; }
    public String  getOrgId()    { return orgId; }
    public String  getNomeOrg()  { return nomeOrg; }
    public String  getNome()     { return nome; }
    public String  getPerfil()   { return perfil; }
}
