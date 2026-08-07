import { defineShikiSetup } from '@slidev/types'

// Muted teals/greens that sit inside the deck's petrol identity,
// instead of shiki's default primary-colored dark-plus.
export default defineShikiSetup(() => ({
  themes: {
    dark: 'vitesse-dark',
    light: 'vitesse-light',
  },
}))
