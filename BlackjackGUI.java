import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

public class BlackjackGUI extends JFrame {

    static class Card {
        int value;
        String name;
        Card(int value, String name) { this.value = value; this.name = name; }
    }

    static String[] CARD_NAMES  = {"Ace","2","3","4","5","6","7","8","9","10","Jack","Queen","King"};
    static int[]    CARD_VALUES = {11,2,3,4,5,6,7,8,9,10,10,10,10};

    ArrayList<Card> deck = new ArrayList<>();
    int numPlayers;
    String[] names;
    int[] balances, bets, aces;
    ArrayList<Card>[] hands;
    ArrayList<Card> dealerHand = new ArrayList<>();
    int dealerAces = 0;
    boolean dealerHidden = true;
    boolean[] busted, stood, blackjack;
    int currentPlayer = 0;

    JLabel messageLabel, dealerTotalLabel;
    JLabel[] balanceLabels, betLabels, totalLabels;
    JButton hitBtn, standBtn, dealBtn;
    JTextField[] betFields;
    JPanel[] handPanels;
    JPanel dealerHandPanel;
    JPanel bottomPanel;
    JPanel bettingRow;

    @SuppressWarnings("unchecked")
    public BlackjackGUI(int numPlayers, String[] names) {
        this.numPlayers = numPlayers;
        this.names = names;
        balances = new int[numPlayers];
        bets = new int[numPlayers];
        aces = new int[numPlayers];
        hands = new ArrayList[numPlayers];
        busted = new boolean[numPlayers];
        stood = new boolean[numPlayers];
        blackjack = new boolean[numPlayers];
        balanceLabels = new JLabel[numPlayers];
        betLabels = new JLabel[numPlayers];
        totalLabels = new JLabel[numPlayers];
        betFields = new JTextField[numPlayers];
        handPanels = new JPanel[numPlayers];
        for (int i = 0; i < numPlayers; i++) { balances[i] = 2500; hands[i] = new ArrayList<>(); }
        buildDeck();
        buildUI();
        showBetting();
    }

    void buildDeck() {
        deck.clear();
        for (int s = 0; s < 4; s++)
            for (int c = 0; c < 13; c++)
                deck.add(new Card(CARD_VALUES[c], CARD_NAMES[c]));
        Collections.shuffle(deck);
    }

    Card draw() {
        if (deck.size() < 15) buildDeck();
        return deck.remove(0);
    }

    void buildUI() {
        setTitle("Budget Blackjack");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 640);
        setMinimumSize(new Dimension(700, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(25, 105, 45));
        setLayout(new BorderLayout(0, 0));

        // ── Dealer section ──
        JPanel dealerSection = new JPanel();
        dealerSection.setOpaque(false);
        dealerSection.setLayout(new BoxLayout(dealerSection, BoxLayout.Y_AXIS));
        dealerSection.setBorder(BorderFactory.createEmptyBorder(18, 0, 10, 0));

        JLabel dealerTitle = new JLabel("DEALER", SwingConstants.CENTER);
        dealerTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        dealerTitle.setForeground(new Color(220, 200, 120));
        dealerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        dealerTotalLabel = new JLabel("Total: ?", SwingConstants.CENTER);
        dealerTotalLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dealerTotalLabel.setForeground(Color.WHITE);
        dealerTotalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dealerHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        dealerHandPanel.setOpaque(false);

        dealerSection.add(dealerTitle);
        dealerSection.add(Box.createVerticalStrut(4));
        dealerSection.add(dealerTotalLabel);
        dealerSection.add(Box.createVerticalStrut(8));
        dealerSection.add(dealerHandPanel);

        add(dealerSection, BorderLayout.NORTH);

        // ── Player sections ──
        JPanel playersRow = new JPanel(new GridLayout(1, numPlayers, 16, 0));
        playersRow.setOpaque(false);
        playersRow.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        for (int i = 0; i < numPlayers; i++) {
            JPanel col = new JPanel();
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
            col.setBackground(new Color(18, 88, 35));
            col.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 150, 60), 2),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));

            JLabel nameL = new JLabel(names[i], SwingConstants.CENTER);
            nameL.setFont(new Font("SansSerif", Font.BOLD, 15));
            nameL.setForeground(new Color(220, 200, 120));
            nameL.setAlignmentX(Component.CENTER_ALIGNMENT);

            balanceLabels[i] = new JLabel("$2500", SwingConstants.CENTER);
            balanceLabels[i].setFont(new Font("SansSerif", Font.PLAIN, 12));
            balanceLabels[i].setForeground(Color.WHITE);
            balanceLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            betLabels[i] = new JLabel("Bet: —", SwingConstants.CENTER);
            betLabels[i].setFont(new Font("SansSerif", Font.PLAIN, 12));
            betLabels[i].setForeground(new Color(180, 220, 180));
            betLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            handPanels[i] = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
            handPanels[i].setOpaque(false);

            totalLabels[i] = new JLabel("", SwingConstants.CENTER);
            totalLabels[i].setFont(new Font("SansSerif", Font.BOLD, 14));
            totalLabels[i].setForeground(Color.WHITE);
            totalLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            col.add(nameL);
            col.add(Box.createVerticalStrut(2));
            col.add(balanceLabels[i]);
            col.add(betLabels[i]);
            col.add(Box.createVerticalStrut(6));
            col.add(handPanels[i]);
            col.add(Box.createVerticalGlue());
            col.add(totalLabels[i]);

            playersRow.add(col);
        }

        add(playersRow, BorderLayout.CENTER);

        // ── Bottom ──
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 20, 16, 20));

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        messageLabel.setForeground(Color.WHITE);
        bottomPanel.add(messageLabel, BorderLayout.NORTH);

        bettingRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 4));
        bettingRow.setOpaque(false);
        for (int i = 0; i < numPlayers; i++) {
            JLabel lbl = new JLabel(names[i] + " $:");
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            betFields[i] = new JTextField("100", 5);
            betFields[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
            bettingRow.add(lbl);
            bettingRow.add(betFields[i]);
        }
        bottomPanel.add(bettingRow, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        btnRow.setOpaque(false);

        hitBtn   = makeBtn("🃏  Hit",   new Color(50, 120, 200));
        standBtn = makeBtn("✋  Stand", new Color(190, 55, 55));
        dealBtn  = makeBtn("♠  Deal",  new Color(45, 155, 75));

        hitBtn.setEnabled(false);
        standBtn.setEnabled(false);

        hitBtn.addActionListener(e -> doHit());
        standBtn.addActionListener(e -> doStand());
        dealBtn.addActionListener(e -> doDeal());

        btnRow.add(hitBtn);
        btnRow.add(standBtn);
        btnRow.add(dealBtn);
        bottomPanel.add(btnRow, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    JButton makeBtn(String text, Color bg) {
        Color hover = bg.brighter();
        JButton b = new JButton(text) {
            boolean on = false;
            { setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
              setFocusPainted(false); setFont(new Font("SansSerif", Font.BOLD, 14));
              setForeground(Color.WHITE); setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              addMouseListener(new MouseAdapter() {
                  public void mouseEntered(MouseEvent e) { on = true;  repaint(); }
                  public void mouseExited(MouseEvent e)  { on = false; repaint(); }
              });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color c = isEnabled() ? (on ? hover : bg) : new Color(90,90,90);
                g2.setPaint(new GradientPaint(0,0,c.brighter(),0,h,c.darker()));
                g2.fillRoundRect(0,0,w-1,h-1,14,14);
                g2.setColor(new Color(255,255,255,50));
                g2.fillRoundRect(2,2,w-4,h/2,12,12);
                g2.setColor(new Color(255,255,255,80));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1,1,w-3,h-3,13,13);
                g2.setFont(getFont()); FontMetrics fm = g2.getFontMetrics();
                int tx=(w-fm.stringWidth(getText()))/2, ty=(h+fm.getAscent()-fm.getDescent())/2;
                g2.setColor(new Color(0,0,0,70)); g2.drawString(getText(),tx+1,ty+1);
                g2.setColor(isEnabled()?Color.WHITE:new Color(170,170,170)); g2.drawString(getText(),tx,ty);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() {
                Dimension d=super.getPreferredSize(); return new Dimension(d.width+16,d.height+4);
            }
        };
        return b;
    }

    JPanel makeCard(String name, boolean faceDown) {
        boolean red = name.equals("Ace")||name.equals("3")||name.equals("5")||
                      name.equals("7")||name.equals("9")||name.equals("Jack")||name.equals("King");
        String abbr = name.equals("Jack")?"J":name.equals("Queen")?"Q":
                      name.equals("King")?"K":name.equals("Ace")?"A":name;
        JPanel c = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth()-4, h=getHeight()-4;
                g2.setColor(new Color(0,0,0,50));
                g2.fillRoundRect(4,4,w,h,12,12);
                if (faceDown) {
                    g2.setColor(new Color(35,65,150));
                    g2.fillRoundRect(0,0,w,h,12,12);
                    g2.setColor(new Color(255,255,255,30));
                    for (int x=0;x<w;x+=8) g2.drawLine(x,0,x,h);
                    for (int y=0;y<h;y+=8) g2.drawLine(0,y,w,y);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0,0,w,h,12,12);
                    g2.setColor(red ? new Color(185,20,20) : new Color(15,15,15));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                    FontMetrics fm=g2.getFontMetrics();
                    g2.drawString(abbr,(w-fm.stringWidth(abbr))/2,(h+fm.getAscent()-fm.getDescent())/2);
                }
                g2.setColor(new Color(180,180,180));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,w,h,12,12);
                g2.dispose();
            }
        };
        c.setPreferredSize(new Dimension(70, 100));
        c.setOpaque(false);
        return c;
    }

    void redrawCards() {
        dealerHandPanel.removeAll();
        for (int i = 0; i < dealerHand.size(); i++)
            dealerHandPanel.add(makeCard(dealerHand.get(i).name, i==1 && dealerHidden));
        dealerHandPanel.revalidate();
        dealerHandPanel.repaint();

        for (int p = 0; p < numPlayers; p++) {
            handPanels[p].removeAll();
            for (Card card : hands[p])
                handPanels[p].add(makeCard(card.name, false));
            handPanels[p].revalidate();
            handPanels[p].repaint();
        }
        updateTotals();
    }

    void updateTotals() {
        for (int i = 0; i < numPlayers; i++) {
            int[] a = {aces[i]};
            totalLabels[i].setText("Total: " + handTotal(hands[i]));
        }
        int dt = dealerHandTotal();
        dealerTotalLabel.setText(dealerHidden ? "Total: ?" : "Total: " + dt);
    }

    int handTotal(ArrayList<Card> hand) {
        int total = 0;
        int aceCount = 0;
        for (Card c : hand) {
            total += c.value;
            if (c.name.equals("Ace")) aceCount++;
        }
        while (total > 21 && aceCount > 0) {
            total -= 10;
            aceCount--;
        }
        return total;
    }

    int dealerHandTotal() {
        int t = 0, a = dealerAces;
        for (Card c : dealerHand) t += c.value;
        while (t > 21 && a > 0) { t -= 10; a--; }
        return t;
    }

    void showBetting() {
        dealerHandPanel.removeAll(); dealerHandPanel.revalidate(); dealerHandPanel.repaint();
        for (int i = 0; i < numPlayers; i++) {
            handPanels[i].removeAll(); handPanels[i].revalidate(); handPanels[i].repaint();
            totalLabels[i].setText("");
        }
        dealerTotalLabel.setText("Total: ?");
        bettingRow.setVisible(true);
        hitBtn.setEnabled(false); standBtn.setEnabled(false); dealBtn.setEnabled(true);
        dealBtn.setText("♠  Deal"); messageLabel.setText("Place your bets!");
        messageLabel.setForeground(new Color(220,200,120));
    }

    void doDeal() {
        for (int i = 0; i < numPlayers; i++) {
            int bet;
            try { bet = Integer.parseInt(betFields[i].getText().trim()); }
            catch (NumberFormatException ex) { msg(names[i] + ": enter a valid number!"); return; }
            if (bet < 1 || bet > balances[i]) { msg(names[i] + ": bet $1–$" + balances[i]); return; }
            bets[i] = bet;
            betLabels[i].setText("Bet: $" + bet);
        }
        bettingRow.setVisible(false);
        for (int i = 0; i < numPlayers; i++) {
            hands[i].clear(); aces[i]=0; busted[i]=false; stood[i]=false; blackjack[i]=false;
        }
        dealerHand.clear(); dealerAces=0; dealerHidden=true; currentPlayer=0;

        for (int i = 0; i < numPlayers; i++) { dealTo(i); dealTo(i); }
        Card d1=draw(); if(d1.name.equals("Ace")) dealerAces++;
        Card d2=draw(); if(d2.name.equals("Ace")) dealerAces++;
        dealerHand.add(d1); dealerHand.add(d2); fixDealerAces();

        redrawCards();

        for (int i = 0; i < numPlayers; i++)
            if (handTotal(hands[i]) == 21) { blackjack[i]=true; stood[i]=true; }

        advanceTurn();
    }

    void dealTo(int i) {
        Card c = draw();
        if (c.name.equals("Ace")) aces[i]++;
        hands[i].add(c);
    }

    void doHit() {
        dealTo(currentPlayer); fixPlayerAces(currentPlayer);
        redrawCards();
        int t = handTotal(hands[currentPlayer]);
        if (t > 21) { busted[currentPlayer]=true; stood[currentPlayer]=true; msg(names[currentPlayer]+" busted with "+t+"!"); advanceTurn(); }
        else if (t == 21) { stood[currentPlayer]=true; advanceTurn(); }
        else msg(names[currentPlayer]+"'s turn — total: "+t);
    }

    void doStand() {
        stood[currentPlayer]=true;
        msg(names[currentPlayer]+" stands at "+handTotal(hands[currentPlayer]));
        advanceTurn();
    }

    void advanceTurn() {
        while (currentPlayer < numPlayers && stood[currentPlayer]) currentPlayer++;
        if (currentPlayer < numPlayers) {
            hitBtn.setEnabled(true); standBtn.setEnabled(true); dealBtn.setEnabled(false);
            int t = handTotal(hands[currentPlayer]);
            msg(blackjack[currentPlayer] ? names[currentPlayer]+" has Blackjack! 🎉" : names[currentPlayer]+"'s turn — total: "+t);
            highlightPlayer();
        } else {
            hitBtn.setEnabled(false); standBtn.setEnabled(false);
            dealerTurn();
        }
    }

    void dealerTurn() {
        dealerHidden=false; redrawCards();
        while (dealerHandTotal() < 17) {
            Card c=draw(); if(c.name.equals("Ace")) dealerAces++;
            dealerHand.add(c); fixDealerAces(); redrawCards();
        }
        updateTotals(); resolveRound();
    }

    void resolveRound() {
        int dt = dealerHandTotal();
        StringBuilder sb = new StringBuilder("<html><center>");
        for (int i = 0; i < numPlayers; i++) {
            int pt=handTotal(hands[i]), bet=bets[i];
            String r;
            if (busted[i]) { balances[i]-=bet; r=names[i]+" busted → -$"+bet; }
            else if (blackjack[i] && dt!=21) { int w=(int)(bet*1.5); balances[i]+=w; r=names[i]+" Blackjack! → +$"+w; }
            else if (blackjack[i]) { r=names[i]+" Push (both 21)"; }
            else if (dt>21) { balances[i]+=bet; r=names[i]+" wins (dealer bust) → +$"+bet; }
            else if (pt>dt) { balances[i]+=bet; r=names[i]+" wins → +$"+bet; }
            else if (pt<dt) { balances[i]-=bet; r=names[i]+" loses → -$"+bet; }
            else { r=names[i]+" Push"; }
            balanceLabels[i].setText("$"+balances[i]);
            sb.append(r).append("<br>");
        }
        sb.append("</center></html>");
        msg(sb.toString()); messageLabel.setForeground(Color.WHITE);

        boolean anyLeft=false;
        for (int b:balances) if(b>0) anyLeft=true;
        if (!anyLeft) { msg("<html><center>Everyone's broke! Game over.</center></html>"); dealBtn.setEnabled(false); return; }

        dealBtn.setText("♠  Next Round"); dealBtn.setEnabled(true);
        dealBtn.removeActionListener(dealBtn.getActionListeners()[0]);
        dealBtn.addActionListener(e -> { dealBtn.removeActionListener(dealBtn.getActionListeners()[0]); dealBtn.addActionListener(ev->doDeal()); showBetting(); });
    }

    void highlightPlayer() {
        // highlight done via border on player panels — access parent of handPanels
    }

    void fixPlayerAces(int i) {
        int t=0; for(Card c:hands[i]) t+=c.value;
        while(t>21&&aces[i]>0){t-=10;aces[i]--;}
    }

    void fixDealerAces() {
        int t=0; for(Card c:dealerHand) t+=c.value;
        while(t>21&&dealerAces>0){t-=10;dealerAces--;}
    }

    void msg(String s) { messageLabel.setText(s); messageLabel.setForeground(Color.WHITE); }

    static void showSetup() {
        JFrame f = new JFrame("Budget Blackjack");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(380, 260);
        f.setLocationRelativeTo(null);
        f.getContentPane().setBackground(new Color(25,105,45));
        f.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8,12,8,12); g.fill=GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("♠ Budget Blackjack ♠", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif",Font.BOLD,22)); title.setForeground(new Color(220,200,120));
        g.gridx=0;g.gridy=0;g.gridwidth=2; f.add(title,g);

        JLabel nl = new JLabel("Players:"); nl.setForeground(Color.WHITE); nl.setFont(new Font("SansSerif",Font.PLAIN,14));
        g.gridy=1;g.gridwidth=1; f.add(nl,g);
        JComboBox<Integer> numBox = new JComboBox<>(new Integer[]{1,2});
        numBox.setFont(new Font("SansSerif",Font.PLAIN,14)); g.gridx=1; f.add(numBox,g);

        JTextField[] nf = new JTextField[2];
        JLabel[] nl2 = new JLabel[2];
        for (int i=0;i<2;i++) {
            nl2[i]=new JLabel("Player "+(i+1)+" name:"); nl2[i].setForeground(Color.WHITE); nl2[i].setFont(new Font("SansSerif",Font.PLAIN,14));
            nf[i]=new JTextField("Player "+(i+1),12); nf[i].setFont(new Font("SansSerif",Font.PLAIN,14));
            g.gridx=0;g.gridy=2+i; f.add(nl2[i],g); g.gridx=1; f.add(nf[i],g);
        }
        nl2[1].setVisible(false); nf[1].setVisible(false);
        numBox.addActionListener(e->{ boolean two=(int)numBox.getSelectedItem()==2; nl2[1].setVisible(two); nf[1].setVisible(two); });

        JButton start = new JButton("Start Game");
        start.setFont(new Font("SansSerif",Font.BOLD,14)); start.setBackground(new Color(45,155,75));
        start.setForeground(Color.WHITE); start.setFocusPainted(false);
        start.setBorder(BorderFactory.createEmptyBorder(10,30,10,30));
        g.gridx=0;g.gridy=4;g.gridwidth=2; f.add(start,g);

        start.addActionListener(e->{
            int n=(int)numBox.getSelectedItem(); String[] ns=new String[n];
            for(int i=0;i<n;i++){ns[i]=nf[i].getText().trim();if(ns[i].isEmpty())ns[i]="Player "+(i+1);}
            f.dispose();
            SwingUtilities.invokeLater(()->{BlackjackGUI game=new BlackjackGUI(n,ns);game.setVisible(true);});
        });
        f.setVisible(true);
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(BlackjackGUI::showSetup); }
}
