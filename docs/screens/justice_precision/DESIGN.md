---
name: Justice & Precision
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#43474f'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#737780'
  outline-variant: '#c3c6d1'
  surface-tint: '#3a5f94'
  primary: '#001e40'
  on-primary: '#ffffff'
  primary-container: '#003366'
  on-primary-container: '#799dd6'
  inverse-primary: '#a7c8ff'
  secondary: '#904d00'
  on-secondary: '#ffffff'
  secondary-container: '#fd8b00'
  on-secondary-container: '#603100'
  tertiary: '#381300'
  on-tertiary: '#ffffff'
  tertiary-container: '#592300'
  on-tertiary-container: '#d8885c'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d5e3ff'
  primary-fixed-dim: '#a7c8ff'
  on-primary-fixed: '#001b3c'
  on-primary-fixed-variant: '#1f477b'
  secondary-fixed: '#ffdcc3'
  secondary-fixed-dim: '#ffb77d'
  on-secondary-fixed: '#2f1500'
  on-secondary-fixed-variant: '#6e3900'
  tertiary-fixed: '#ffdbca'
  tertiary-fixed-dim: '#ffb690'
  on-tertiary-fixed: '#341100'
  on-tertiary-fixed-variant: '#723610'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  h1:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  h2:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
    letterSpacing: -0.01em
  h3:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
    letterSpacing: '0'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
    letterSpacing: '0'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
    letterSpacing: '0'
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
    letterSpacing: '0'
  label-caps:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: 0.05em
  button:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '600'
    lineHeight: '1'
    letterSpacing: 0.01em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  margin-mobile: 16px
  margin-desktop: 32px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
  section-padding: 48px
---

## Brand & Style

This design system is built on the pillars of **Authority, Transparency, and Efficiency**. Designed specifically for the legal and bailiff sector, the UI avoids unnecessary decorative elements in favor of a **Corporate Modern** aesthetic. The style prioritizes clarity of information and ease of navigation to reduce cognitive load during complex legal workflows.

The visual direction uses high whitespace ("breathability") to convey a sense of calm and organization. By balancing the weight of the navy blue with a clean, airy background, the interface establishes a professional tone that is both formal and accessible. The mobile-first approach ensures that legal professionals can manage cases and view documents with the same precision on the go as they do at a desk.

## Colors

The color palette is anchored by **Navy Blue (#003366)**, representing stability, law, and institutional trust. This color is used for primary actions, navigation headers, and critical text to establish hierarchy.

**Vibrant Orange (#FF8C00)** serves as the strategic accent. It is used sparingly for call-to-action buttons, notifications, and status indicators that require immediate attention without breaking the formal tone. 

The neutral palette utilizes slate grays for secondary text and borders, while the background remains a very light off-white to reduce eye strain and enhance the "breathability" of the layout.

## Typography

This design system utilizes **Inter** for its exceptional readability and neutral, systematic appearance. The type scale is optimized for high-density legal data while maintaining significant vertical rhythm. 

- **Headlines:** Use Bold weights to ground the page and define clear sections.
- **Body Text:** Uses a 1.5x to 1.6x line height to ensure long case descriptions and legal texts are legible.
- **Labels:** Small caps with slight tracking are used for metadata and table headers to distinguish them from actionable data.

## Layout & Spacing

The layout follows a **fluid grid** model with a strict 8px baseline. This ensures vertical rhythm across all device sizes. 

For mobile, a single-column layout with 16px side margins is standard. On desktop, a 12-column grid is used with significant outer margins to keep content centered and focused. Space is used as a functional tool: larger gaps (24px+) are used to separate distinct legal entities or case sections, while tighter spacing (8px) groups related form fields or metadata.

## Elevation & Depth

Visual hierarchy is established through **Ambient Shadows** and **Tonal Layers**. This design system avoids heavy gradients, opting for soft, diffused shadows to lift active elements from the background.

- **Level 0 (Background):** #F8FAFC - The base canvas.
- **Level 1 (Cards/Surface):** White (#FFFFFF) with a very soft shadow (0px 2px 4px rgba(0, 51, 102, 0.05)).
- **Level 2 (Active/Hover):** White (#FFFFFF) with an elevated shadow (0px 8px 16px rgba(0, 51, 102, 0.08)).
- **Level 3 (Modals/Overlays):** White (#FFFFFF) with a deep, focused shadow (0px 12px 24px rgba(0, 51, 102, 0.12)).

Subtle 1px borders in a light gray (#E2E8F0) are used in conjunction with shadows to maintain structure in high-density data views.

## Shapes

The shape language utilizes a **Rounded (8px-12px)** approach to soften the formality of the application, making it feel modern and user-friendly.

- **Small Components (8px):** Buttons, Input fields, and Chips.
- **Large Components (12px):** Cards, Modals, and main content containers.
- **Full Rounding:** Progress bars and status badges (pills).

Consistent corner radii help the application feel cohesive and "engineered," reinforcing the precision required in legal software.

## Components

### Buttons
- **Primary:** Navy Blue background, white text. Bold and authoritative.
- **Secondary:** White background, Navy Blue border and text.
- **Accent (CTA):** Vibrant Orange background, white text. Used for "Start Enforcement" or "Submit" actions.

### Input Fields
- Outlined style with a 1px border (#CBD5E1). 
- On focus: Border thickens to 2px and changes to Navy Blue with a subtle blue outer glow.
- Labels are positioned above the field in `body-sm` weight.

### Cards
- White background, 12px corner radius, and Level 1 shadow. 
- Internal padding should be generous (min 20px) to maintain the "breathability" requirement.

### Chips & Status Badges
- Small pill shapes with low-opacity background tints of the status color (e.g., light green for "Resolved", light red for "Overdue"). Text is a darker version of the status color.

### Lists & Tables
- Bordered-bottom rows with no side borders. 
- High contrast between the header (Navy Blue text) and row data (Slate Gray text).

### Document Preview
- A specific component for viewing legal PDFs, featuring a dark gray container background to contrast with the white "paper" of the document.