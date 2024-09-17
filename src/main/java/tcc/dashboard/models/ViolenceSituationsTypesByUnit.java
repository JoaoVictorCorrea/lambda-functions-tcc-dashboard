package tcc.dashboard.models;

import java.util.Objects;

public class ViolenceSituationsTypesByUnit {

    private Unit unit;
    private int qtdFisica;
    private int qtdPsicologica;
    private int qtdAbusoOuViolenciaSexual;
    private int qtdAtoInfracional;
    private int qtdNegligenciaContraCrianca;

    public ViolenceSituationsTypesByUnit() {}

    public ViolenceSituationsTypesByUnit(Unit unit, int qtdFisica, int qtdPsicologica,
                                         int qtdAbusoOuViolenciaSexual, int qtdAtoInfracional, int qtdNegligenciaContraCrianca) {
        this.unit = unit;
        this.qtdFisica = qtdFisica;
        this.qtdPsicologica = qtdPsicologica;
        this.qtdAbusoOuViolenciaSexual = qtdAbusoOuViolenciaSexual;
        this.qtdAtoInfracional = qtdAtoInfracional;
        this.qtdNegligenciaContraCrianca = qtdNegligenciaContraCrianca;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public int getQtdFisica() {
        return qtdFisica;
    }

    public void setQtdFisica(int qtdFisica) {
        this.qtdFisica = qtdFisica;
    }

    public int getQtdAbusoOuViolenciaSexual() {
        return qtdAbusoOuViolenciaSexual;
    }

    public void setQtdAbusoOuViolenciaSexual(int qtdAbusoOuViolenciaSexual) {
        this.qtdAbusoOuViolenciaSexual = qtdAbusoOuViolenciaSexual;
    }

    public int getQtdPsicologica() {
        return qtdPsicologica;
    }

    public void setQtdPsicologica(int qtdPsicologica) {
        this.qtdPsicologica = qtdPsicologica;
    }

    public int getQtdAtoInfracional() {
        return qtdAtoInfracional;
    }

    public void setQtdAtoInfracional(int qtdAtoInfracional) {
        this.qtdAtoInfracional = qtdAtoInfracional;
    }

    public int getQtdNegligenciaContraCrianca() {
        return qtdNegligenciaContraCrianca;
    }

    public void setQtdNegligenciaContraCrianca(int qtdNegligenciaContraCrianca) {
        this.qtdNegligenciaContraCrianca = qtdNegligenciaContraCrianca;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViolenceSituationsTypesByUnit that = (ViolenceSituationsTypesByUnit) o;
        return qtdFisica == that.qtdFisica && qtdPsicologica == that.qtdPsicologica && qtdAbusoOuViolenciaSexual == that.qtdAbusoOuViolenciaSexual && qtdAtoInfracional == that.qtdAtoInfracional && qtdNegligenciaContraCrianca == that.qtdNegligenciaContraCrianca && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, qtdFisica, qtdPsicologica, qtdAbusoOuViolenciaSexual, qtdAtoInfracional, qtdNegligenciaContraCrianca);
    }
}
