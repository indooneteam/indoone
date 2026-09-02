window.IndooneZoho = (() => {
  const SERVICES = [
    { key: 'mail', name: 'Zoho Mail', tokens: ['mail', 'zohomail'] },
    { key: 'crm', name: 'Zoho CRM', tokens: ['crm', 'zohocrm'] },
    { key: 'books', name: 'Zoho Books', tokens: ['books', 'zohobooks'] },
    { key: 'desk', name: 'Zoho Desk', tokens: ['desk', 'zohodesk'] },
    { key: 'one', name: 'Zoho One', tokens: ['one', 'zohoone'] },
    { key: 'projects', name: 'Zoho Projects', tokens: ['projects', 'zohoprojects'] },
    { key: 'creator', name: 'Zoho Creator', tokens: ['creator', 'zohocreator'] },
    { key: 'workdrive', name: 'Zoho WorkDrive', tokens: ['workdrive', 'zohoworkdrive'] },
    { key: 'cliq', name: 'Zoho Cliq', tokens: ['cliq', 'zohocliq'] },
    { key: 'meeting', name: 'Zoho Meeting', tokens: ['meeting', 'zohomeeting'] }
  ];

  function clean(value) {
    return String(value || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  function detect(issuer, label) {
    const issuerText = clean(issuer);
    const labelText = clean(label);
    const combined = `${issuerText} ${labelText}`;
    const isZoho = issuerText === 'zoho' || issuerText.includes('zoho') || labelText.startsWith('zoho');
    if (!isZoho) return null;
    const service = SERVICES.find(item => item.tokens.some(token => combined.includes(token)));
    return {
      provider: 'Zoho',
      service: service?.name || 'Zoho Account',
      serviceKey: service?.key || 'accounts'
    };
  }

  return { detect, services: SERVICES };
})();
