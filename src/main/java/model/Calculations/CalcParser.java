
package model.Calculations;


public class CalcParser {
    
    public CalcParser( String  exp ){
        
        for (int i = 0; i < exp.length(); i++){
            char c = exp.charAt(i);        

            process(c);
            
        }
        
    }
    
    private void process(char c){
        
        switch(c){
            case '(':
                break;
            case ')':
                break;
            case ' ':
        }

    }
}


/*



(((CHWRT - CHWST) * CHWFLO) / 24) < 0 ? 0 : ((CHWRT - CHWST) * CHWFLO) / 24	


(
  (Ton == null or CHWFLO == null or ChillerkW == null)
  or
  (Ton == 0 and CHWFLO > MinimumChilledWaterFlow / 2 and ChillerkW > 50)
)
  ? (null)
  : (
      (Ton < 0)
      ? (0)
      : (
          (Ton > TotalCapacity * 1.5)
          ? (TotalCapacity * 1.5)
          : (
              (CHWFLO < MinimumChilledWaterFlow / 2)
              ? (0)
              : (Ton)
            )
        )
    )

avg(TotalTon)

CLGREQD and not OptimizationDisabled and 
  (CDWSTSPNotOptimized or CHWDPSPNotOptimized or CH1CHWSTSPNotOptimized or CH2CHWSTSPNotOptimized or CH3CHWSTSPNotOptimized or CT1SPDNotOptimized or CT2SPDNotOptimized or PCHWP1SPDNotOptimized or PCHWP2SPDNotOptimized or PCHWP3SPDNotOptimized or SCHWP1SPDNotOptimized or SCHWP2SPDNotOptimized or SCHWP3SPDNotOptimized or CDWP1SPDNotOptimized or CDWP2SPDNotOptimized or CDWP3SPDNotOptimized)	

CH1kW + CH2kW + CH3kW + PCHWP1kW + PCHWP2kW + PCHWP3kW + SCHWP1kW + SCHWP2kW + SCHWP3kW + CDWP1kW + CDWP2kW + CDWP3kW + CT1kW + CT2kW

*/