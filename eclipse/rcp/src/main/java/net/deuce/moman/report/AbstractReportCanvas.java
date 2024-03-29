package net.deuce.moman.report;

import net.deuce.moman.entity.ServiceProvider;
import net.deuce.moman.entity.model.EntityEvent;
import net.deuce.moman.entity.model.EntityListener;
import net.deuce.moman.entity.model.account.Account;
import net.deuce.moman.entity.service.account.AccountService;

import org.eclipse.birt.chart.computation.DataPointHints;
import org.eclipse.birt.chart.device.ICallBackNotifier;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.event.StructureSource;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.ActionType;
import org.eclipse.birt.chart.model.attribute.ActionValue;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.CallBackValue;
import org.eclipse.birt.chart.model.attribute.TriggerCondition;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.model.attribute.impl.CallBackValueImpl;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.data.Action;
import org.eclipse.birt.chart.model.data.Trigger;
import org.eclipse.birt.chart.model.data.impl.ActionImpl;
import org.eclipse.birt.chart.model.data.impl.TriggerImpl;
import org.eclipse.birt.chart.render.IActionRenderer;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateRangeCombo;
import org.eclipse.swt.widgets.Display;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractReportCanvas extends Canvas implements
		ICallBackNotifier {

	private DateRange dateRange = DateRange.currentMonth;
	private IDeviceRenderer renderer;
	private Chart chart;
	private GeneratedChartState state;
	private Image cachedImage;

    public AbstractReportCanvas(Composite parent, final DateRangeCombo combo,
			int style) {
		super(parent, style);

		try {
			PluginSettings ps = PluginSettings.instance();
			renderer = ps.getDevice("dv.SWT");
			renderer.setProperty(IDeviceRenderer.UPDATE_NOTIFIER, this);
		} catch (ChartException pex) {
			pex.printStackTrace();
			throw new RuntimeException(pex);
		}

		combo.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (dateRange != combo.getDateRange()) {
					dateRange = combo.getDateRange();
					regenerateChart();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {

				Composite co = (Composite) e.getSource();
				final Rectangle rect = co.getClientArea();
				renderer.getDisplayServer().setGraphicsContext(e.gc);

				if (cachedImage == null) {
					buildChart();
					drawToCachedImage(rect);
				}

				e.gc.drawImage(cachedImage, 0, 0,
						cachedImage.getBounds().width,
						cachedImage.getBounds().height, 0, 0, rect.width,
						rect.height);
			}
		});

        AccountService accountService = ServiceProvider.instance().getAccountService();
        accountService.addEntityListener(new EntityListener<Account>() {

			public void entityRemoved(EntityEvent<Account> event) {
				regenerateChart();
			}

			public void entityChanged(EntityEvent<Account> event) {
				regenerateChart();
			}

			public void entityAdded(EntityEvent<Account> event) {
				regenerateChart();
			}
		});
	}

	protected abstract Chart doCreateChart();

	public DateRange getDateRange() {
		return dateRange;
	}

	private Chart getChart() {
		if (chart == null) {
			chart = doCreateChart();
		}
		return chart;
	}

	protected Trigger createTrigger(ActionValue actionValue,
			ActionType actionType, TriggerCondition condition) {
		Action clickAction = ActionImpl.create(actionType, actionValue);
		return TriggerImpl.create(condition, clickAction);
	}

	protected void addTrigger(Series series) {
		ActionValue actionValue = CallBackValueImpl.create(getClass()
				.getCanonicalName());
		series.getTriggers().add(
				createTrigger(actionValue, ActionType.CALL_BACK_LITERAL,
						TriggerCondition.ONCLICK_LITERAL));
	}

	public void callback(Object event, Object source, CallBackValue value) {
		handleCallback((StructureSource) source, value,
				(DataPointHints) ((StructureSource) source).getSource());
	}

	public Chart getDesignTimeModel() {
		return getChart();
	}

	public Chart getRunTimeModel() {
		return getChart();
	}

	public Object peerInstance() {
		return this;
	}

	public void regenerateChart() {
		cachedImage = null;
		chart = null;
		redraw();
	}

	public void repaintChart() {
		redraw();
	}

	private void buildChart() {
		Point size = getSize();
		Bounds bo = BoundsImpl.create(0, 0, size.x, size.y);
		int resolution = renderer.getDisplayServer().getDpiResolution();
		bo.scale(72d / resolution);
		try {
			Generator gr = Generator.instance();
			state = gr.build(renderer.getDisplayServer(), getChart(), bo, null,
					null, null);
			IActionRenderer actionRenderer = getActionRenderer();
			if (actionRenderer != null) {
				state.getRunTimeContext().setActionRenderer(actionRenderer);
			}
		} catch (ChartException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	protected IActionRenderer getActionRenderer() {
		return null;
	}

	private void drawToCachedImage(Rectangle size) {
		GC gc = null;
		try {
			if (cachedImage != null) {
				cachedImage.dispose();
			}
			cachedImage = new Image(Display.getCurrent(), size);

			gc = new GC(cachedImage);
			renderer.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);

			Generator gr = Generator.instance();
			gr.render(renderer, state);
		} catch (ChartException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			if (gc != null) {
				gc.dispose();
			}
		}
	}

	protected abstract DataSetResult createDataSet(boolean expense);

	protected void handleCallback(StructureSource source, CallBackValue value,
			DataPointHints dataPointHints) {
	}
}
