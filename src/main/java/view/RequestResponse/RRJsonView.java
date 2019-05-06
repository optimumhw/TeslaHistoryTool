package view.RequestResponse;

import model.RestClient.EnumCallType;
import model.RestClient.RRObj;
import org.json.JSONObject;

public class RRJsonView extends javax.swing.JFrame {

    private final RRObj rr;
    
    public RRJsonView(RRObj rr) {
        
        initComponents();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        this.rr = rr;
       
        String areaText = "";
        if (rr.getCallType() == EnumCallType.RESPONSE) {
            if( rr.getPayload() != null ){
            String payload = rr.getPayload();
            if (payload.length() > 0) {
                try{
                JSONObject json = new JSONObject(payload);
                areaText = json.toString(4);
                }
                catch( Exception ex){
                    areaText = payload;
                }
            }
            }
            else{
               areaText = "empty payload";
            }

        } else {
            String[] pieces = rr.getURL().split("[\\s\\?\\&]+");
            String text = "";
            for (String s : pieces) {
                text = text + s + System.lineSeparator();
            }
            if (rr.getPayload().length() > 0) {
                
                String temp = rr.getPayload();
                if( !temp.startsWith("{")){
                    temp = "{ \"payload\": " + temp + "}";
                    
                }
                JSONObject json = new JSONObject(temp);
                text += json.toString(4);
            }
            areaText = text;
        }
        
        this.jTextAreaPayload.setText( areaText );  
    }
    
    private void showPopUp( java.awt.event.MouseEvent e ){
        if( e.isPopupTrigger() ){
            new PopUpMenuForRRTextArea( e, this.jTextAreaPayload );
        }
        
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPanePanel = new javax.swing.JScrollPane();
        jTextAreaPayload = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextAreaPayload.setColumns(20);
        jTextAreaPayload.setRows(5);
        jTextAreaPayload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTextAreaPayloadMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTextAreaPayloadMouseReleased(evt);
            }
        });
        jScrollPanePanel.setViewportView(jTextAreaPayload);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPanePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPanePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextAreaPayloadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextAreaPayloadMousePressed
        showPopUp( evt );
    }//GEN-LAST:event_jTextAreaPayloadMousePressed

    private void jTextAreaPayloadMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextAreaPayloadMouseReleased
        showPopUp( evt );
    }//GEN-LAST:event_jTextAreaPayloadMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPanePanel;
    private javax.swing.JTextArea jTextAreaPayload;
    // End of variables declaration//GEN-END:variables
}
