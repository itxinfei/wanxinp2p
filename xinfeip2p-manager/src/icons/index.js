import SvgIcon from '@/components/SvgIcon' // svg component

// register globally
export function setupIcons(app) {
  app.component('svg-icon', SvgIcon)
}