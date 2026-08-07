import { defineMermaidSetup } from '@slidev/types'

// Match the deck's ink/petrol identity; mermaid's default light theme would
// glow like a lightbox on the dark ground.
export default defineMermaidSetup(() => ({
  theme: 'base',
  themeVariables: {
    background: '#101214',
    primaryColor: '#1e2226',
    primaryTextColor: '#edeff1',
    primaryBorderColor: '#047090',
    secondaryColor: '#024b60',
    secondaryTextColor: '#edeff1',
    tertiaryColor: '#1e2226',
    tertiaryTextColor: '#edeff1',
    lineColor: '#6aaec3',
    textColor: '#b2b7bb',
    // sequence diagrams
    actorBkg: '#1e2226',
    actorBorder: '#047090',
    actorTextColor: '#edeff1',
    signalColor: '#6aaec3',
    signalTextColor: '#b2b7bb',
    labelBoxBkgColor: '#024b60',
    labelTextColor: '#edeff1',
    noteBkgColor: '#1e2226',
    noteTextColor: '#b2b7bb',
    noteBorderColor: '#3d4349',
  },
}))
