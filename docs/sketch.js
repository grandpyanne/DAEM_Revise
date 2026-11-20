// Draws the note diagrams: the same drawing language as tools/sketches.py and TopicSketches.kt.
function drawSketch(c, src, k, ink, textInk){
 const st=(w,dash)=>{c.lineWidth=w*k;c.setLineDash(dash?[6*k,5*k]:[]);c.stroke()};
 c.strokeStyle=ink;c.fillStyle=ink;c.lineCap='round';c.lineJoin='round';
 for(const raw of src.split(';')){const cmd=raw.trim();if(!cmd)continue;const op=cmd[0];
  if(op=='T'||op=='t'){const m=cmd.match(/^[Tt]\s+(\S+)\s+(\S+)\s+(\S+)\s+(.*)$/);const [x,y,s]=[+m[1],+m[2],+m[3]];c.fillStyle=textInk;c.font=`600 ${s*k}px Roboto, sans-serif`;c.textAlign=op=='T'?'center':'left';c.textBaseline='middle';c.fillText(m[4],x*k,y*k);c.fillStyle=ink;continue}
  const n=cmd.slice(1).trim().split(/\s+/).filter(Boolean).map(Number);
  const line=(w,d)=>{c.beginPath();c.moveTo(n[0]*k,n[1]*k);c.lineTo(n[2]*k,n[3]*k);st(w,d)};
  const head=(x1,y1,x2,y2)=>{const a=Math.atan2(y2-y1,x2-x1);c.beginPath();c.moveTo(x2*k,y2*k);c.lineTo((x2-7*Math.cos(a-.45))*k,(y2-7*Math.sin(a-.45))*k);c.moveTo(x2*k,y2*k);c.lineTo((x2-7*Math.cos(a+.45))*k,(y2-7*Math.sin(a+.45))*k);st(1.4)};
  switch(op){
   case'L':line(2.2);break;case'l':line(1.2);break;case'W':line(3.4);break;case'-':line(1.2,true);break;
   case'A':line(1.4);head(n[0],n[1],n[2],n[3]);break;
   case'D':{line(1.2);head(n[2],n[3],n[0],n[1]);head(n[0],n[1],n[2],n[3]);break}
   case'R':c.beginPath();c.rect(n[0]*k,n[1]*k,n[2]*k,n[3]*k);st(2.2);break;
   case'r':c.beginPath();c.rect(n[0]*k,n[1]*k,n[2]*k,n[3]*k);st(1.2,true);break;
   case'O':c.beginPath();c.arc(n[0]*k,n[1]*k,n[2]*k,0,7);st(2.2);break;
   case'o':c.beginPath();c.arc(n[0]*k,n[1]*k,n[2]*k,0,7);st(1.2,true);break;
   case'F':c.beginPath();c.arc(n[0]*k,n[1]*k,n[2]*k,0,7);c.fill();break;
   case'E':c.beginPath();c.ellipse(n[0]*k,n[1]*k,n[2]*k,n[3]*k,0,0,7);st(2.2);break;
   case'a':c.beginPath();c.arc(n[0]*k,n[1]*k,n[2]*k,n[3]*Math.PI/180,(n[3]+n[4])*Math.PI/180,n[4]<0);st(2.2);break;
   case'P':case'G':case'H':{c.beginPath();c.moveTo(n[0]*k,n[1]*k);for(let i=2;i<n.length;i+=2)c.lineTo(n[i]*k,n[i+1]*k);if(op!='P')c.closePath();if(op=='H'){c.globalAlpha=0.25;c.fill();c.globalAlpha=1}else st(2.2);break}
   case'B':case'b':c.beginPath();c.moveTo(n[0]*k,n[1]*k);c.bezierCurveTo(n[2]*k,n[3]*k,n[4]*k,n[5]*k,n[6]*k,n[7]*k);st(op=='B'?2.2:1.2,op=='b');break;
   case'Z':{const [x0,y0,x1,y1,cnt]=n;const dx=x1-x0,dy=y1-y0,len=Math.hypot(dx,dy),ux=dx/len,uy=dy/len,px=-uy*6,py=ux*6;c.beginPath();c.moveTo(x0*k,y0*k);for(let i=1;i<=cnt;i++){const t=i/(cnt+1);const s=i%2?1:-1;c.lineTo((x0+dx*t+px*s)*k,(y0+dy*t+py*s)*k)}c.lineTo(x1*k,y1*k);st(2.2);break}
   case'K':{const [x0,y,x1,cnt]=n;const w=(x1-x0)/cnt;c.beginPath();for(let i=0;i<cnt;i++){c.moveTo((x0+i*w)*k,y*k);c.arc((x0+i*w+w/2)*k,y*k,w/2*k,Math.PI,0)}st(2.2);break}
   case'g':{const[x,y]=n;c.beginPath();c.moveTo((x-10)*k,y*k);c.lineTo((x+10)*k,y*k);c.moveTo((x-6)*k,(y+4)*k);c.lineTo((x+6)*k,(y+4)*k);c.moveTo((x-2)*k,(y+8)*k);c.lineTo((x+2)*k,(y+8)*k);st(1.8);break}
  }}
}
