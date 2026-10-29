// In-browser stand-in for the Spring Boot API (same routes and workflow rules). Used ONLY for the published demo.
(function(){
const RULES=[['DRAFT','SUBMITTED',['SUBMITTER','ADMIN']],['SUBMITTED','APPROVED',['REVIEWER','ADMIN']],['SUBMITTED','REJECTED',['REVIEWER','ADMIN']],['REJECTED','DRAFT',['SUBMITTER','ADMIN']],['APPROVED','PUBLISHED',['ADMIN']]];
const seed=[['Machine Learning for Crop Yield Prediction','A. Rao, S. Mehta','Thesis','Computer Engineering',2025,'machine learning, agriculture','PUBLISHED'],
['Low-Cost Water Quality Sensor Network','P. Nair','Journal Article','Electronics',2024,'iot, water, sensors','PUBLISHED'],
['Seismic Retrofit of Masonry Buildings','K. Iyer, D. Shah','Conference Paper','Civil Engineering',2023,'seismic, masonry','APPROVED'],
['Graph Databases for Library Catalogues','N. Patni','Dissertation','Computer Engineering',2026,'graph, catalogue, search','SUBMITTED'],
['Thermal Behaviour of Recycled Concrete Blocks','R. Gupta','Journal Article','Civil Engineering',2026,'concrete, thermal, recycling','DRAFT'],
['Open Access Policy Compliance in Indian Universities','M. Desai','Report','Library Science',2022,'open access, policy','REJECTED']];
let nextId=1;const db=seed.map(s=>({id:nextId++,title:s[0],authors:s[1],abstractText:'',recordType:s[2],department:s[3],publicationYear:s[4],keywords:s[5],status:s[6],handleId:'IRP/SEED000'+(nextId-1),versionNo:1,updatedAt:new Date().toISOString()}));
const mayMove=(f,t,r)=>RULES.some(x=>x[0]===f&&x[1]===t&&x[2].includes(r));
const canCreate=r=>r==='SUBMITTER'||r==='ADMIN';
const canEdit=(s,r)=>canCreate(r)&&(s==='DRAFT'||s==='REJECTED');
const view=(r,role)=>({...r,editable:canEdit(r.status,role),actions:RULES.filter(x=>x[0]===r.status&&x[2].includes(role)).map(x=>x[1])});
const J=(o,s=200)=>new Response(JSON.stringify(o),{status:s,headers:{'Content-Type':'application/json'}});
const E=(s,m)=>J({message:m,status:s},s);
const real=window.fetch.bind(window);
window.fetch=async function(url,opts={}){
  if(typeof url!=='string'||!(url.startsWith('/api')||url.startsWith('/actuator')))return real(url,opts);
  await new Promise(r=>setTimeout(r,60));
  const u=new URL(url,location.origin),p=u.pathname,m=(opts.method||'GET').toUpperCase(),role=((opts.headers||{})['X-Role']||'VIEWER').toUpperCase();
  const body=opts.body?JSON.parse(opts.body):{};
  if(p==='/actuator/health')return J({status:'UP'});
  if(p==='/api/info')return J({environment:'demo',version:'1.0-demo'});
  if(p==='/api/dashboard'){const bs={DRAFT:0,SUBMITTED:0,APPROVED:0,REJECTED:0,PUBLISHED:0},bd={};db.forEach(r=>{bs[r.status]++;bd[r.department]=(bd[r.department]||0)+1});return J({total:db.length,byStatus:bs,byDepartment:bd})}
  if(p==='/api/records'&&m==='GET'){const q=(u.searchParams.get('q')||'').toLowerCase(),s=u.searchParams.get('status');
    return J(db.filter(r=>(!s||r.status===s)&&[r.title,r.authors,r.keywords,r.department].some(x=>(x||'').toLowerCase().includes(q))).sort((a,b)=>b.id-a.id).map(r=>view(r,role)))}
  if(p==='/api/records'&&m==='POST'){if(!canCreate(role))return E(403,'Role '+role+' cannot create records.');
    if(!body.title||!body.title.trim()||!body.authors||!body.authors.trim())return E(400,'Title and authors are required.');
    const r={id:nextId++,...body,status:'DRAFT',handleId:'IRP/'+Math.random().toString(16).slice(2,10).toUpperCase(),versionNo:1,updatedAt:new Date().toISOString()};db.push(r);return J(view(r,role),201)}
  let mm=p.match(/^\/api\/records\/(\d+)(\/transition)?$/);
  if(mm){const r=db.find(x=>x.id==mm[1]);if(!r)return E(404,'Record '+mm[1]+' was not found.');
    if(mm[2]&&m==='POST'){if(!RULES.some(x=>x[0]===r.status&&x[1]===body.to))return E(409,'A record cannot move from '+r.status+' to '+body.to+'.');
      if(!mayMove(r.status,body.to,role))return E(403,'Role '+role+' cannot move a record to '+body.to+'.');r.status=body.to;return J(view(r,role))}
    if(m==='PUT'){if(!canEdit(r.status,role))return E(403,'Role '+role+' cannot edit a record in status '+r.status+'.');Object.assign(r,body,{versionNo:r.versionNo+1});return J(view(r,role))}
    return J(view(r,role))}
  return E(404,'Not found');
};
})();
