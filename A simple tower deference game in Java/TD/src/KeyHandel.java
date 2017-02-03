import java.awt.event.*;
import java.awt.*;
public class KeyHandel implements MouseMotionListener, MouseListener{

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton()==1){
			Map.store.click(e.getButton());
		} else {
			Map.store.click2((e.getX())/52,(e.getY())/52);
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public void mouseDragged(MouseEvent e) {
		//Map.mse= new Point((e.getX())+((Frame.size.width-Map.myWidth)/2), (e.getY())+((Frame.size.height - (Map.myHeight))-(Frame.size.width-Map.myWidth)/2));
	}


	public void mouseMoved(MouseEvent e) {
		Map.mse= new Point((e.getX())-((Frame.size.width-Map.myWidth)/2), (e.getY())-((Frame.size.height - (Map.myHeight))-(Frame.size.width-Map.myWidth)/2));
		
	}
	

	
	
}
 