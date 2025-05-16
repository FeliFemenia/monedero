package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void poner(double monto) {
    this.verificarMonto(monto);

    if (this.obtenerDepositosA(LocalDate.now()).size() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    this.agregarMovimiento(LocalDate.now(), monto, true);
  }

  public void sacar(double monto) {
    this.verificarMonto(monto);

    if (this.saldo - monto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }

    var limite = 1000 - this.getMontoExtraidoA(LocalDate.now());
    if (monto > limite) {
      throw new MaximoExtraccionDiarioException(
          "No puede extraer mas de $ " + 1000 + " diarios, " + "límite: " + limite);
    }

    this.agregarMovimiento(LocalDate.now(), monto, false);
  }

  public void verificarMonto(double monto) {
    if (monto <= 0) {
      throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void modificarSaldo(Movimiento movimiento) {
    if (movimiento.fueDepositado(LocalDate.now())) {
      this.saldo = this.saldo + movimiento.getMonto();
    } else {
      this.saldo = this.saldo - movimiento.getMonto();
    }
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    var movimiento = new Movimiento(fecha, cuanto, esDeposito);
    this.modificarSaldo(movimiento);
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return this.obtenerExtraccionesA(fecha).stream().mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> obtenerDepositosA(LocalDate fecha) {
    return this.movimientos.stream()
        .filter(movimiento -> movimiento.fueDepositado(LocalDate.now())).toList();
  }

  public List<Movimiento> obtenerExtraccionesA(LocalDate fecha) {
    return this.movimientos.stream().filter(movimiento -> !movimiento.fueDepositado(fecha)).toList();
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

//  //public void setSaldo(double saldo) {
//    this.saldo = saldo;
//  }

}
