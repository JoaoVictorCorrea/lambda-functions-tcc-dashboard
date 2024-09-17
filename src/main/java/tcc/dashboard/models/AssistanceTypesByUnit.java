package tcc.dashboard.models;

import java.util.Objects;

public class AssistanceTypesByUnit {

    private Unit unit;
    private int qtdAtendimentoRecepcao;
    private int qtdAtendimentoSocial;
    private int qtdAtendimentoAtualizacaoCadUnico;
    private int qtdAtendimentoCadastramentoCadUnico;
    private int qtdVisitaDomiciliar;

    public AssistanceTypesByUnit() {}

    public AssistanceTypesByUnit(Unit unit, int qtdAtendimentoRecepcao, int qtdAtendimentoSocial,
                                 int qtdAtendimentoAtualizacaoCadUnico, int qtdAtendimentoCadastramentoCadUnico, int qtdVisitaDomiciliar) {
        this.unit = unit;
        this.qtdAtendimentoRecepcao = qtdAtendimentoRecepcao;
        this.qtdAtendimentoSocial = qtdAtendimentoSocial;
        this.qtdAtendimentoAtualizacaoCadUnico = qtdAtendimentoAtualizacaoCadUnico;
        this.qtdAtendimentoCadastramentoCadUnico = qtdAtendimentoCadastramentoCadUnico;
        this.qtdVisitaDomiciliar = qtdVisitaDomiciliar;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public int getQtdAtendimentoRecepcao() {
        return qtdAtendimentoRecepcao;
    }

    public void setQtdAtendimentoRecepcao(int qtdAtendimentoRecepcao) {
        this.qtdAtendimentoRecepcao = qtdAtendimentoRecepcao;
    }

    public int getQtdAtendimentoSocial() {
        return qtdAtendimentoSocial;
    }

    public void setQtdAtendimentoSocial(int qtdAtendimentoSocial) {
        this.qtdAtendimentoSocial = qtdAtendimentoSocial;
    }

    public int getQtdAtendimentoAtualizacaoCadUnico() {
        return qtdAtendimentoAtualizacaoCadUnico;
    }

    public void setQtdAtendimentoAtualizacaoCadUnico(int qtdAtendimentoAtualizacaoCadUnico) {
        this.qtdAtendimentoAtualizacaoCadUnico = qtdAtendimentoAtualizacaoCadUnico;
    }

    public int getQtdAtendimentoCadastramentoCadUnico() {
        return qtdAtendimentoCadastramentoCadUnico;
    }

    public void setQtdAtendimentoCadastramentoCadUnico(int qtdAtendimentoCadastramentoCadUnico) {
        this.qtdAtendimentoCadastramentoCadUnico = qtdAtendimentoCadastramentoCadUnico;
    }

    public int getQtdVisitaDomiciliar() {
        return qtdVisitaDomiciliar;
    }

    public void setQtdVisitaDomiciliar(int qtdVisitaDomiciliar) {
        this.qtdVisitaDomiciliar = qtdVisitaDomiciliar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssistanceTypesByUnit that = (AssistanceTypesByUnit) o;
        return qtdAtendimentoRecepcao == that.qtdAtendimentoRecepcao && qtdAtendimentoSocial == that.qtdAtendimentoSocial && qtdAtendimentoAtualizacaoCadUnico == that.qtdAtendimentoAtualizacaoCadUnico && qtdAtendimentoCadastramentoCadUnico == that.qtdAtendimentoCadastramentoCadUnico && qtdVisitaDomiciliar == that.qtdVisitaDomiciliar && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, qtdAtendimentoRecepcao, qtdAtendimentoSocial, qtdAtendimentoAtualizacaoCadUnico, qtdAtendimentoCadastramentoCadUnico, qtdVisitaDomiciliar);
    }
}
