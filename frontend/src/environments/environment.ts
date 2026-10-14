// La publishable/anon key di Supabase è pubblica per progetto: può stare nel repo.
// La service_role key invece NON deve MAI finire nel frontend.
export const environment = {
  apiUrl: 'http://localhost:8080/api',
  supabaseUrl: 'https://zmbbqspsnqakmzhgqicb.supabase.co',
  supabaseAnonKey: 'sb_publishable_KOkH1QpqTSUPTa7OcEkEyQ_aututenx' // Supabase > Project Settings > API
};
