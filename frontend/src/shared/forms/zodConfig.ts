import { z } from 'zod';

// Zod probes `new Function` at load; a strict Content-Security-Policy logs that probe as a violation even though it is caught.
z.config({ jitless: true });
